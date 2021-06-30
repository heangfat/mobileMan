#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket;
import threading;
import struct;
import cv2
import time
import os
import numpy

class 攝像頭:
	def __init__(self, resolution = (640, 480), host = ("", 7999)):
		self.resolution = resolution;
		self.host = host;
		self.setSocket(self.host);
		self.img_quality = 15
	def setImageResolution(self, resolution):
		self.resolution = resolution;
	def setHost(self, host):
		self.host = host;
	def setSocket(self, host):
		self.socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM);
		self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR,1);
		self.socket.bind(self.host);
		self.socket.listen(5);
		print("Server running on port:%d" % host[1]);
	def recv_config(self,client):
		info = struct.unpack("lhh",client.recv(12))# 原作8
		if info[0]>911:        #print(info[0])
			self.img_quality=int(info[0])-911
			self.resolution=list(self.resolution)
			self.resolution[0]=info[1]
			self.resolution[1]=info[2]
			self.resolution=tuple(self.resolution)
			return 1
		else :
			return 0
	def _processConnection(self, client,addr):
		if(self.recv_config(client)==0):
			return
		camera = cv2.VideoCapture(0)
		encode_param=[int(cv2.IMWRITE_JPEG_QUALITY),self.img_quality]
		f = open("video_info.txt", 'a+')
		print("Got connection from %s:%d" % (addr[0], addr[1]),file=f);
		print("分辨率：%d × %d"%(self.resolution[0],self.resolution[1]),file=f)
		print("打開攝像頭",file=f)
		print("連接開始時刻:%s"%time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time())),file=f)
		f.close()
		while True:
			time.sleep(0.13)
			(grabbed, self.img) = camera.read()
			self.img  = cv2.resize(self.img,self.resolution)
			result, imgencode = cv2.imencode('.jpg',self.img,encode_param)
			img_code = numpy.array(imgencode)
			self.imgdata  = img_code.tobytes()
			try:
				client.send(struct.pack("lhh",len(self.imgdata), self.resolution[0],self.resolution[1])+self.imgdata); #發送圖片信息(圖片長度、分辨率、圖片內容)
			except:
				f = open("video_info.txt", 'a+')
				print("%s:%d disconnected!" % (addr[0], addr[1]),file=f);
				print("連接結束時刻:%s"%time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time())),file=f)
				print("****************************************",file=f)
				camera.release()
				f.close()
				return
	def run(self):
		while True:
			client,addr = self.socket.accept()
			clientThread = threading.Thread(target = self._processConnection, args = (client, addr, )); #有客戶端連接時，新開線程處理 
			clientThread.start()
def main():
	cam = 攝像頭()
	cam.run()
if __name__ == "__main__":
	main()