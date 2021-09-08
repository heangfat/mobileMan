sTCPserv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)#;sTCPserv.bind(('',7888));sTCPserv.listen(5)# 文字.
sTCPvideo = socket.socket(socket.AF_INET, socket.SOCK_STREAM);sTCPvideo.bind((本機地址,7999));sTCPvideo.listen(5)

def 發視訊(client, useTCP=True):
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
def 复挂號信(sock, addr):
	print('%s:%s 求連，受之。' % addr)
	sock.send('歡迎'.encode('utf-8'))
	while True:
		data = sock.recv(1024)
		time.sleep(1)
		if not data or data.decode('utf-8') == '敔':
			break
		sock.send(('喏，%s！' % data.decode('utf-8')).encode('utf-8'))
	sock.close();print('與 %s:%s 斷了。' % addr)
