#!/usr/bin/sh
sudo /usr/sbin/iptables -A INPUT -s 172.16.1.0/24 -j DROP
sudo /usr/sbin/iptables -A OUTPUT -d 172.16.1.0/24 -j DROP