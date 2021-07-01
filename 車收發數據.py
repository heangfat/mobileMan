#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket
import threading
import json
import numpy as 算
import cv2
import time
import struct

本機地址 = '10.96.45.36'# 壞'10.25.57.175'
收信端口 = 6666
sUDPrcv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM);sUDPrcv.bind((本機地址,收信端口))
遙控器地址 = '127.0.0.1';遙控器端口 = 6677
sUDPsd = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)#;sUDPsd.bind((遙控器地址,遙控器端口))
sTCPvideo = socket.socket(socket.AF_INET, socket.SOCK_STREAM);sTCPvideo.bind((本機地址,7999));sTCPvideo.listen(5)
口令 = '我是主平板。'
收得 = ''
亂數 = 算.random.default_rng(1)
狀態 = {
	"自平衡臺":[0, 2, 79],
	"底盤狀態":{"速度":1.2},
	"距離":[0,0,0,0,0,0]
}
圖像屬性 = {
	"img_quality":15,
	"resolution":(640,480)
}
#print(f'經 {收信端口} 系聯…')
def 發信(套接):
	while True:
		if 遙控器地址 != '127.0.0.1':
			狀態["距離"] = 算.round(亂數.random(6)*10,4).tolist()
			#print(f'發至{遙控器地址}:{遙控器端口}')# 收聽 ROS 話題
			套接.sendto(json.dumps(狀態).encode('utf-8'), (遙控器地址,遙控器端口))
def 發視訊(client, addr):
	camera = cv2.VideoCapture(0)
	編碼參數 = [int(cv2.IMWRITE_JPEG_QUALITY),圖像屬性["img_quality"]]
	while True:
		if True:#遙控器地址 != '127.0.0.1':
			time.sleep(0.13)
			rval, frame = camera.read()
			frame = cv2.resize(frame, 圖像屬性["resolution"])
			result, imgencode = cv2.imencode('.jpg', frame, 編碼參數)
			imgdata = 算.array(imgencode).tobytes()
			try:
				client.send(struct.pack("lhh",len(imgdata), 圖像屬性["resolution"][0],圖像屬性["resolution"][1])+imgdata)#;print(len(imgdata))
				#UDP：套接.sendto(struct.pack("lhh",len(imgdata), 圖像屬性["resolution"][0],圖像屬性["resolution"][1])+imgdata, (遙控器地址,7999))
			except:
				camera.release()
def 收信(套接):
	global 遙控器地址,收得
	while True:
		data, addr = 套接.recvfrom(1024)
		if data.decode('utf-8')[0:6] == 口令:
			遙控器地址 = addr[0]#;遙控器端口 = addr[1]
			收得 = data.decode('utf-8')[6:]
			# 發布 ROS 話題
			print(f"{addr[0]}:{addr[1]} 發來：{data.decode('utf-8')}")
綫程收 = threading.Thread(target=收信, args=(sUDPrcv,))
綫程發 = threading.Thread(target=發信, args=(sUDPsd,))
綫程收.start();綫程發.start()
#綫程收.join();綫程發.join()
while True:
	client,addr = sTCPvideo.accept()
	clientThread = threading.Thread(target = 發視訊, args = (client, addr, ))
	clientThread.start()
