iptables -A INPUT -s 172.16.1.0/24 -j DROP
iptables -A INPUT -d 172.16.1.0/24 -j DROP