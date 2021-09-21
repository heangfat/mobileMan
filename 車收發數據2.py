#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket
import threading
import json
import numpy as np
import cv2
import time
import struct
import random
import rospy
from geometry_msgs.msg import Twist
from std_msgs.msg import String



rospy.init_node("cmdvel_publisher")
localAddress = '192.168.10.124'#'10.96.45.36'
rcvMsgPort = 6566
sUDPrcv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM);sUDPrcv.bind(('',rcvMsgPort))
remoteCtrlAddr = '127.0.0.1';remoteCtrlPort = 6677
sUDPsd = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)#;sUDPsd.bind((remoteCtrlAddr,remoteCtrlPort))

pswd = 'I am M'
receivedUDPstr = ''# 收得
#亂數 = np.random.default_rng(1)
mmStatus = {
	"forkLift":[0, 2, 79],
	"base":{"速度":1.2},
	"distance":[0,0,0,0,0,0]
}
imgProperty = {
	"img_quality":15,
	"resolution":(1920,1080)
}

class Subscriber():
	def __init__(self):
		self.message = Twist()
		rospy.Subscriber("/cmd_vel",Twist, self.callback)
		return self.message
	
	def callback(self,msg):
		self.message = msg
		

#print(f'經 {收信端口} 系聯…')
def UDPsendTXT(sckt):# Subscribe ROS topic of system components status & send out through UDP. 發信
	while True:
		if remoteCtrlAddr != '127.0.0.1':
			#mmStatus["distance"] = np.round(亂數.random(6)*10,4).tolist()# Temporarily generate random float number list to imitate sensor readings in ROS topic.
			#print(f'Send to {remoteCtrlAddr}:{remoteCtrlPort}')
			# Subsribe ROS topic here. The ROS topic contains the status of system components. Use the subscribed values to update “mmStatus”.
			cmd_velMessage = Subscriber()
			sckt.sendto(json.dumps(mmStatus).encode('utf-8'), (remoteCtrlAddr,remoteCtrlPort))# Send the json string to the pad

def UDPreceiveTXT(sckt):# Receive string of commands sent from pad. Sort & publish to ROS topics.
	global remoteCtrlAddr,receivedUDPstr
	while True:
		data, addr = sckt.recvfrom(1024)
		if data.decode('utf-8')[0:6] == pswd:
			remoteCtrlAddr = addr[0]#;remoteCtrlPort = addr[1]
			receivedUDPstr = data.decode('utf-8')[6:]# Indices 0~5 are for verification. Only parse from 6.
			# Publish ROS topic here according to the received string. E.g.
			# if receivedUDPstr == 'mvFwd_1'
			twist_msg = Twist()
			twist_msg.linear.x = 0
			twist_msg.angular.z = 0
			pub = rospy.Publisher("/cmd_vel",Twist,queue_size = 10)
			pub.publish(twist_msg)
			#	pub.publish(…)
			#print(f"{addr[0]}:{addr[1]} 發來：{data.decode('utf-8')}")
class AnswerTxt :
	def __init__(self,rcvPort):
		#global sTCPserv
		self.TCPsvSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.TCPsvSock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR,1)
		self.TCPsvSock.bind(('',rcvPort));self.TCPsvSock.listen(5)
		#sTCPserv.bind(('',rcvPort));sTCPserv.listen(5)
		self.imgQuality = 15;self.resolution = imgProperty["resolution"]#(640,480)
		self.tSN = 0
	def reply(self, sock, addr):
		print('%s:%s 求連，受之。' % addr)
		sock.send('歡迎'.encode('utf-8'))
		while True:
			time.sleep(0.1)
			try:
				data = sock.recv(1024)
				if not data:
					continue
				if data.decode('utf-8') == '敔':
					break
				msgLength = random.randrange(123490000,123490100);
				print(self.tSN,struct.pack('l',msgLength),msgLength,'---',data.decode('utf-8'))#,end=' ■ '
				sock.send(struct.pack('lhh',msgLength,self.resolution[1],self.resolution[0]))#('喏，%s！' % data.decode('utf-8')).encode('utf-8')
			except:
				print(self.tSN,'　　〼該幀未捕獲或未發出。')
			finally:
				self.tSN += 1
		sock.close();print('與 %s:%s 斷了。' % addr)
	def response(self):
		#global 對方址口
		while True:
			sock, clientAddrPort = self.TCPsvSock.accept()
			t = threading.Thread(target=self.reply, args=(sock, clientAddrPort))
			t.start()
threadReceive = threading.Thread(target=UDPreceiveTXT, args=(sUDPrcv,))
threadSend = threading.Thread(target=UDPsendTXT, args=(sUDPsd,))
threadReceive.start();threadSend.start()
#threadReceive.join();threadSend.join()
class scanCameras:
	def __init__(self, cam_preset_num=10):
		self.cam_preset_num = cam_preset_num
	def get_cams(self):
		cnt = 0;availSN = []
		for device in range(0, self.cam_preset_num):
			try:
				stream = cv2.VideoCapture(device)
				grabbed = stream.grab()
				stream.release()
				if not grabbed:
					continue
			finally:
				pass
			cnt += 1
			availSN.append(device)
		return (cnt,availSN)
cam = scanCameras(20)
avlbCams = cam.get_cams();print('可用攝像頭：',avlbCams[1])
class AnswerVideo(AnswerTxt):
	def reply(self, sock, addr):
		print('%s:%s asking for connection, accept。' % addr)
		camera = cv2.VideoCapture(avlbCams[1][0])
		encode_param=[int(cv2.IMWRITE_JPEG_QUALITY), self.imgQuality]
		while True:
			time.sleep(0.13)
			try:
				(grabbed, self.img) = camera.read()
				self.img  = cv2.resize(self.img,self.resolution)
				result, imgencode = cv2.imencode('.jpg',self.img,encode_param)
				img_code = np.array(imgencode)
				self.imgdata  = img_code.tobytes()
				#cv2.imshow('服務端',self.img)
				data = sock.recv(1024)
				if not data:
					continue
				msgLength = len(self.imgdata)
				print(self.tSN,msgLength,'---')#,struct.pack('l',msgLength),data.decode('utf-8') ,end=' ■ '
			except Exception as e:
				print(self.tSN,'　　〼該幀未捕獲。錯因',e)
				continue
			try:
				sock.send(struct.pack('lhh',msgLength,self.resolution[1],self.resolution[0])+self.imgdata)#('喏，%s！' % data.decode('utf-8')).encode('utf-8')
			except:
				print(self.tSN,'　　〼該幀未發出。')
			finally:
				self.tSN += 1
		sock.close();print('與 %s:%s 斷了。' % addr)
		camera.release()
tcpvideo = AnswerVideo(7891)
tcpvideo.response()
tcptxt = AnswerTxt(7888)
rospy.spin()
#tcptxt.response()
