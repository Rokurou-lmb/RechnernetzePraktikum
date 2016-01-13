//This Script should reset the firewall and reallow all necessary connections
iptables -F
iptables -A INPUT -s localhost -j ACCEPT
iptables -A INPUT -t localhost -j ACCEPT
iptables -A INPUT -s 141.22.192.100 -j ACCEPT
iptables -A INPUT -t 141.22.192.100 -j ACCEPT
iptables -A INPUT -s filercpt.informatik.haw-hamburg.de -j ACCEPT
iptables -A INPUT -t filercpt.informatik.haw-hamburg.de -j ACCEPT