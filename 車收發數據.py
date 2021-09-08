#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket
import threading
import json
import numpy as 算
import cv2
import time
import struct
import random

本機地址 = '192.168.10.124'#'10.96.45.36'
收信端口 = 6566
sUDPrcv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM);sUDPrcv.bind(('',收信端口))
遙控器地址 = '127.0.0.1';遙控器端口 = 6677
sUDPsd = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)#;sUDPsd.bind((遙控器地址,遙控器端口))

口令 = '我是主平板。'
receivedUDPstr = ''# 收得
亂數 = 算.random.default_rng(1)
mmStatus = {
	"forkLift":[0, 2, 79],
	"base":{"速度":1.2},
	"distance":[0,0,0,0,0,0]
}
圖像屬性 = {
	"img_quality":15,
	"resolution":(1920,1080)
}
#print(f'經 {收信端口} 系聯…')
def UDPsendTXT(套接):# Subscribe ROS topic of system components status & send out through UDP. 發信
	while True:
		if 遙控器地址 != '127.0.0.1':
			mmStatus["distance"] = 算.round(亂數.random(6)*10,4).tolist()# Temporarily generate random float number list to imitate sensor readings in ROS topic.
			#print(f'Send to {遙控器地址}:{遙控器端口}')
			# Subsribe ROS topic here. The ROS topic contains the status of system components. Use the subscribed values to update “mmStatus”.
			套接.sendto(json.dumps(mmStatus).encode('utf-8'), (遙控器地址,遙控器端口))# Send the json string to the pad

def UDPreceiveTXT(套接):# Receive string of commands sent from pad. Sort & publish to ROS topics.
	global 遙控器地址,receivedUDPstr
	while True:
		data, addr = 套接.recvfrom(1024)
		if data.decode('utf-8')[0:6] == 口令:
			遙控器地址 = addr[0]#;遙控器端口 = addr[1]
			receivedUDPstr = data.decode('utf-8')[6:]# Indices 0~5 are for verification. Only parse from 6.
			# Publish ROS topic here according to the received string. E.g.
			# if receivedUDPstr == 'mvFwd_1':
			#	pub.publish(…)
			print(f"{addr[0]}:{addr[1]} 發來：{data.decode('utf-8')}")
class 畣复文字 :
	def __init__(self,受端口):
		#global sTCPserv
		self.TCPsvSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.TCPsvSock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR,1)
		self.TCPsvSock.bind(('',受端口));self.TCPsvSock.listen(5)
		#sTCPserv.bind(('',受端口));sTCPserv.listen(5)
		self.畫質 = 15;self.resolution = 圖像屬性["resolution"]#(640,480)
		self.流水號 = 0
	def 畣复(self, sock, addr):
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
				信長 = random.randrange(123490000,123490100);
				print(self.流水號,struct.pack('l',信長),信長,'---',data.decode('utf-8'),end=' ■ ')
				sock.send(struct.pack('lhh',信長,self.resolution[1],self.resolution[0]))#('喏，%s！' % data.decode('utf-8')).encode('utf-8')
			except:
				print(self.流水號,'　　〼該幀未捕獲或未發出。')
			finally:
				self.流水號 += 1
		sock.close();print('與 %s:%s 斷了。' % addr)
	def 響應(self):
		#global 對方址口
		while True:
			sock, 對方址口 = self.TCPsvSock.accept()
			t = threading.Thread(target=self.畣复, args=(sock, 對方址口))
			t.start()
綫程收 = threading.Thread(target=UDPreceiveTXT, args=(sUDPrcv,))
綫程發 = threading.Thread(target=UDPsendTXT, args=(sUDPsd,))
綫程收.start();綫程發.start()
#綫程收.join();綫程發.join()
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
cam = scanCameras(100)
avlbCams = cam.get_cams();print('可用攝像頭：',avlbCams[1])
class 畣复視訊(畣复文字):
	def 畣复(self, sock, addr):
		print('%s:%s 求連，受之。' % addr)
		camera = cv2.VideoCapture(avlbCams[1][0])
		encode_param=[int(cv2.IMWRITE_JPEG_QUALITY), self.畫質]
		while True:
			time.sleep(0.13)
			try:
				(grabbed, self.img) = camera.read()
				self.img  = cv2.resize(self.img,self.resolution)
				result, imgencode = cv2.imencode('.jpg',self.img,encode_param)
				img_code = 算.array(imgencode)
				self.imgdata  = img_code.tobytes()
				#cv2.imshow('服務端',self.img)
				data = sock.recv(1024)
				if not data:
					continue
				信長 = len(self.imgdata)
				print(self.流水號,信長,'---',end=' ■ ')#,struct.pack('l',信長),data.decode('utf-8')
			except Exception as e:
				print(self.流水號,'　　〼該幀未捕獲。錯因',e)
				continue
			try:
				sock.send(struct.pack('lhh',信長,self.resolution[1],self.resolution[0])+self.imgdata)#('喏，%s！' % data.decode('utf-8')).encode('utf-8')
			except:
				print(self.流水號,'　　〼該幀未發出。')
			finally:
				self.流水號 += 1
		sock.close();print('與 %s:%s 斷了。' % addr)
		camera.release()
tcpvideo = 畣复視訊(7891)
tcpvideo.響應()
tcptxt = 畣复文字(7888)
#tcptxt.響應()
