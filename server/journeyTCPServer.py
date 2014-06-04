#!/usr/bin/env python2

import socket
import datetime
import struct
import argparse
import time
import json
import os, sys

def parseArguments():
	parser = argparse.ArgumentParser()
	parser.add_argument("--directory", "-d", action="store", dest="outputDir", default=os.getcwd()+"/data/", help="directory to save to (default: ./data)")
	parser.add_argument("--quiet", "-q", action="store_true", dest="quiet", help="no output to stdout")
	
	return parser.parse_args()


def main(argv):	
	args = parseArguments()
	
	try:
		if not os.path.isdir(args.outputDir):
			os.makedirs(args.outputDir)
	except OSerror as e:
		if not args.quiet:
			print "Could not create directory "+ str(e)
		sys.exit(1)
	
	if not os.access(args.outputDir, os.W_OK):
		if not args.quiet:
			print "Output directory is not writable"
		sys.exit(1)
	
	
	try:
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
		sock.bind(("0.0.0.0", 1338))
		sock.listen(5)
	except socket.error as e:
		if not args.quiet:
			print "Could not set up socket: "+ str(e)
		if not sock == None:
			sock.close()
		sys.exit(1)
	
	while True:
		conn, addr = sock.accept()
		f = open(args.outputDir+str(datetime.datetime.now()), "w")
		
		data = conn.recv(1024)
		
		while data:
			f.write(data)
			data = conn.recv(1024)
		
		f.close()
		conn.close()
		
		print "Connection handled"
		
		

if __name__ == "__main__":
	try:
		main(sys.argv)
	except KeyboardInterrupt:			
		sys.exit(0)
