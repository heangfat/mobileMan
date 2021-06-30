#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket
import threading;
import struct;  import os;
import time;  import sys;
import numpy
import cv2
import re
class webCamConnect:
	def __init__(self, resolution = [640,480], remoteAddress = ("10.96.45.36", 7999), windowName = "video"):
		self.remoteAddress = remoteAddress;
		self.resolution = resolution;
		self.name = windowName;
		self.mutex = threading.Lock();
		self.src=911+15
		self.interval=0
		self.path=os.getcwd()
		self.img_quality = 15
	def _setSocket(self):
		self.socket=socket.socket(socket.AF_INET, socket.SOCK_STREAM);
		self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1);
	def connect(self):
		self._setSocket();
		self.socket.connect(self.remoteAddress);
	def _processImage(self):
		self.socket.send(struct.pack("lhh",self.src,self.resolution[0],self.resolution[1]));
		while(1):
			info = struct.unpack("lhh",self.socket.recv(12))# 原作8
			bufSize = info[0];
			if bufSize:
				try:
					self.mutex.acquire();
					self.buf=b''
					tempBuf=self.buf;
					while(bufSize):                 #循環讀取到一張圖片的長度
						tempBuf = self.socket.recv(bufSize);
						bufSize -= len(tempBuf);
						self.buf += tempBuf;
						data = numpy.fromstring(self.buf,dtype='uint8')
						self.image=cv2.imdecode(data,1)
						cv2.imshow(self.name,self.image)
				except:
					 print("接收失敗")
					 pass;
				finally:
					self.mutex.release();
					if cv2.waitKey(10) == 27:
						self.socket.close()
						cv2.destroyAllWindows()
						print("放棄連接")
						break
	def getData(self, interval):
		showThread=threading.Thread(target=self._processImage);
		showThread.start();
		if interval != 0:   # 非0則啟動保存截圖到本地的功能
			saveThread=threading.Thread(target=self._savePicToLocal,args = (interval, ));
			saveThread.setDaemon(1);
			saveThread.start();
	def setWindowName(self, name):
		self.name = name
	def setRemoteAddress(remoteAddress):
		self.remoteAddress = remoteAddress
	def _savePicToLocal(self, interval):
		while(1):
			try:
				self.mutex.acquire();
				path=os.getcwd() + "\\" + "savePic";
				if not os.path.exists(path):
					os.mkdir(path);
				cv2.imwrite(path + "\\" + time.strftime("%Y%m%d-%H%M%S",
						time.localtime(time.time())) + ".jpg",self.image)
			except:
				pass
			finally:
				self.mutex.release()
				time.sleep(interval)
	def check_config(self):
		path=os.getcwd()
		print(path)
		if os.path.isfile(r'%s\video_config.txt'%path) is False:
			f = open("video_config.txt", 'w+')
			print("w = %d,h = %d" %(self.resolution[0],self.resolution[1]),file=f)
			print("IP is %s:%d" %(self.remoteAddress[0],self.remoteAddress[1]),file=f)
			print("Save pic flag:%d" %(self.interval),file=f)
			print("image's quality is:%d,range(0~95)"%(self.img_quality),file=f)
			f.close()
			print("初始化配置");
		else:
			f = open("video_config.txt", 'r+')
			tmp_data=f.readline(50)#1 resolution
			num_list=re.findall(r"\d+",tmp_data)
			self.resolution[0]=int(num_list[0])
			self.resolution[1]=int(num_list[1])
			tmp_data=f.readline(50)#2 ip,port
			num_list=re.findall(r"\d+",tmp_data)
			str_tmp="%d.%d.%d.%d" %(int(num_list[0]),int(num_list[1]),int(num_list[2]),int(num_list[3]))
			self.remoteAddress=(str_tmp,int(num_list[4]))
			tmp_data=f.readline(50)#3 savedata_flag
			self.interval=int(re.findall(r"\d",tmp_data)[0])
			tmp_data=f.readline(50)#3 savedata_flag
			#print(tmp_data)
			self.img_quality=int(re.findall(r"\d+",tmp_data)[0])
			#print(self.img_quality)
			self.src=911+self.img_quality
			f.close()
			print("讀取配置")
def main():
	print("創建連接...")
	cam = webCamConnect();
	cam.check_config()
	print("像素為:%d * %d"%(cam.resolution[0],cam.resolution[1]))
	print("目標ip為%s:%d"%(cam.remoteAddress[0],cam.remoteAddress[1]))
	cam.connect();
	cam.getData(cam.interval);
if __name__ == "__main__":
	main()

# s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
# for data in ['黃鐘'.encode('utf-8'), '大呂'.encode('utf-8'), '太蔟'.encode('utf-8')]:
# 	s.sendto(data, ('10.96.45.36', 6666))
# 	print(s.recv(1024).decode('utf-8'))
# s.close()