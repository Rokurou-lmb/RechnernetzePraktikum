//this routes to subnet 192.168.18.0/24 over the Router at 192.168.17.2 
route -add -net 192.168.18.0/24 192.168.17.2 
route -add -net fd32:6de0:1f69:18::2/64 fd32:6de0:1f69:17::2/64 