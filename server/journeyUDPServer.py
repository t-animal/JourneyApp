#!/usr/bin/env python2

import socket
import datetime
import struct
import argparse
import time
import json
import os, sys

jsonFile = None
binaryFile = None

def parseArguments():
	parser = argparse.ArgumentParser()
	parser.add_argument("--json", "-j", action="store", dest="json", default=None, help="filename to json output save to")
	parser.add_argument("--binary", "-b", action="store", dest="binary", default=None, help="filename to save binary output to")
	parser.add_argument("--quiet", "-q", action="store_true", dest="quiet", help="no output to stdout")
	parser.add_argument("--interval", "-i", action="store", type=int, default=10, dest="interval", help="minimum save interval for the json file in seconds")
	
	return parser.parse_args()

def printToAll(output):
	if not jsonFile == None and int(time.time())-lastPrintJson > args.json_interval:
		lastPrintJson = int(time.time())
		jsonFile.write(output)


def main(argv):
	global jsonFile, binaryFile
	
	args = parseArguments()
	
	lastPrintJson = 0
	sock = None
	users = {}
	
	try:
		sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		sock.bind(("0.0.0.0", 1338))
	except socket.error as e:
		if not args.quiet:
			print "Could not set up socket: "+ str(e)
		if not sock == None:
			sock.close()
		sys.exit(1)
	
	
	try:
		if not args.binary == None:
			binaryFile = open(args.binary, "w+b")
		if not args.json == None:
			jsonFile = open(args.json, "w+")
	except IOError as e:
		if not binaryFile == None:
			binaryFile.close()
		if not jsonFile == None:
			jsonFile.close()
		sock.close()
		
		if not args.quiet:
			print "Could not open file: "+ str(e)
		sys.exit(1)
	
	
	while True:
		data, addr = sock.recvfrom(1024)
		userIdLen = ord(data[0])
		userId = data[1:userIdLen+1]
		lat, lon, acc, sentTime, caught = struct.unpack(">ddfq?", data[userIdLen+1:])
		
		if not userId in users.keys():
			users[userId] = []
		
		users[userId].append({"lat":lat, "lon": lon, "acc": acc, "time": sentTime, "caught": caught})
		
		
		if not args.quiet:
			print "[{}, {: <26s}] ".format(str(datetime.datetime.now()), str(addr)),
			print "User: {} Lat: {:0<3.7f} Lon: {:0<3.7f} Acc:{:<2.0f} Time:{} Caught:{}".\
				format(userId, lat, lon, acc, datetime.datetime.fromtimestamp(sentTime), caught)
		
		
		if not binaryFile == None:
			binaryFile.write(str(data))
			binaryFile.flush()
		
		
		if not jsonFile == None and int(time.time())-lastPrintJson > args.interval:
			lastPrintJson = int(time.time())
			jsonFile.seek(0)
			jsonFile.truncate(0)
			jsonFile.write("newData(")
			jsonFile.write(json.dumps(users))
			jsonFile.write(");");
			jsonFile.flush()
			os.fsync(jsonFile.fileno())

if __name__ == "__main__":
	try:
		main(sys.argv)
	except KeyboardInterrupt:
		if not binaryFile == None:
			binaryFile.close()
		if not jsonFile == None:
			jsonFile.close()
			
		sys.exit(0)
