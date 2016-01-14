#!/usr/bin/sh
sudo /usr/sbin/iptables -I OUTPUT -p tcp --dport http -j DROP
sudo /usr/sbin/iptables -I OUTPUT -d www.dmi.dk -p tcp --dport http -j ACCEPT