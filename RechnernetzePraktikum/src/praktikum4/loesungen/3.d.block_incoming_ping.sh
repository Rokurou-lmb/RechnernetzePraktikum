iptables -A INPUT -s 172.16.1.0/24 -p icmp --icmp-type echo-request -j DROP
iptables -A OUTPUT -d 172.16.1.0/24 -p icmp --icmp-type echo-request -j ACCEPT
