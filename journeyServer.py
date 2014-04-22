#!/usr/bin/env python2

import socket
import datetime
import struct

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

sock.bind(("0.0.0.0", 1338))

print "set up"

while True:
		data, addr = sock.recvfrom(1024)
		userIdLen = ord(data[0])
		userId = data[1:userIdLen+1]
		lat, lon, acc = struct.unpack(">ddf", data[userIdLen+1:])
		
		print "[{}, {: <26s}] ".format(str(datetime.datetime.now()), str(addr)),
		print "User: {} Lat: {:0<3.7f} Lon: {:0<3.7f} Acc:{} ".format(userId, lat, lon, acc)
