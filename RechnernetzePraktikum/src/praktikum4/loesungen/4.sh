#!/usr/bin/sh

# Router RNS1:
sudo /sbin/route add -net 192.168.18.0/24 gw 192.168.17.2
sudo /sbin/route add -A inet6 fd32:6de0:1f69:18::2/64 gw fd32:6de0:1f69:17::2

sudo /sbin/route add -net 192.168.17.0/24 gw 192.168.18.2
sudo /sbin/route add -A inet6 fd32:6de0:1f69:17::2/64 gw fd32:6de0:1f69:18::2

# ISDN-Router:
sudo /sbin/route add -net 192.168.18.0/24 gw 192.168.17.1
sudo /sbin/route add -net 192.168.17.0/24 gw 192.168.18.1
