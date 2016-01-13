iptables -A OUTPUT -d www.dmi.dk -p http --dport 51000 -j ACCEPT //might have to manually resolve the ip beforehand
iptables -A INPUT -s 172.16.1.0/24 -j DROP