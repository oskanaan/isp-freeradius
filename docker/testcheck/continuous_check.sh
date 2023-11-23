#!/bin/sh
while true; do
  echo "Testing RADIUS Authentication..."
#  echo "User-Name=testuser,MD5-Password=testpassword" | radclient -x $RADIUS_HOST:1812 auth testing123
  echo "User-Name=testuser,MD5-Password=testpassword,Acct-Status-Type=Start,Acct-Session-Id=12345" | radclient -x $RADIUS_HOST:1813 acct testing123
  echo "User-Name=testuser,Acct-Status-Type=Interim-Update,Acct-Session-Id=12345,Acct-Input-Octets=1024,Acct-Output-Octets=2048" | radclient -x $RADIUS_HOST:1813 acct testing123
  echo "User-Name=testuser,Acct-Status-Type=Stop,Acct-Session-Id=12345,Acct-Input-Octets=4096,Acct-Output-Octets=8192" | radclient -x $RADIUS_HOST:1813 acct testing123

  sleep 10  # Pause for 10 seconds before next attempt
done
