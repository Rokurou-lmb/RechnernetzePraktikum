#!/usr/bin/sh
sudo /usr/sbin/iptables -A INPUT -s 172.16.1.0/24 -p tcp --dport 51000 -j ACCEPT
sudo /usr/sbin/iptables -A INPUT -s 172.16.1.0/24 -j DROP