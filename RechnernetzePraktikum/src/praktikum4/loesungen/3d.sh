#!/usr/bin/sh
sudo /usr/sbin/iptables -I INPUT -s 172.16.1.0/24 -p icmp --icmp-type echo-request -j DROP
