#!/usr/bin/python3

import sys
import paramiko

k = paramiko.RSAKey.from_private_key_file("path to your private key")
c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
print("connecting")
c.connect( hostname = "ibis-host-dev.cit.nih.gov", username = "tomcat", pkey = k )
print("connected")

stdin , stdout, stderr = c.exec_command("touch /opt/apache-tomcat-2nd/paramiko-x.txt")
print(stdout.read())

c.close()