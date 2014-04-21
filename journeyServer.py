#!/usr/bin/env python2

import socket

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

sock.bind(("0.0.0.0", 1338))

print "set up"

while True:
		data, addr = sock.recvfrom(1024)
		print "connection handled"
		print "Data: {} ".format(data)
