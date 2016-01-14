#!/usr/bin/sh
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -p tcp --syn -j DROP
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -p tcp --tcp-flags SYN,ACK -j ACCEPT