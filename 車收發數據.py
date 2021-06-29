#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket
import threading
import json
import numpy as 算

本機地址 = '10.96.45.36'# 壞'10.25.57.175'
視訊端口 = 6666
sUDPrcv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM);sUDPrcv.bind((本機地址,視訊端口))
遙控器地址 = '127.0.0.1';遙控器端口 = 6677
sUDPsd = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)#;sUDPsd.bind((遙控器地址,遙控器端口))
口令 = '我是主平板。'
收得 = ''
亂數 = 算.random.default_rng(1)
發出 = {
	"自平衡臺":[0, 2, 79],
	"底盤狀態":{"速度":1.2},
	"距離":[0,0,0,0,0,0]
}
print(f'經 {視訊端口} 系聯…')
def 發信(套接):
	while True:
		if 遙控器地址 != '127.0.0.1':
			發出["距離"] = 算.round(亂數.random(6)*10,4).tolist()
			#print(f'發至{遙控器地址}:{遙控器端口}')# 收聽 ROS 話題
			套接.sendto(json.dumps(發出).encode('utf-8'), (遙控器地址,遙控器端口))
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
綫程收.join();綫程發.join()