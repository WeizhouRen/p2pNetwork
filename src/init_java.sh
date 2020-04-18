
#Xterm -hold -title "Peer 2" -e "java p2p init 2 4 5 30" &
#Xterm -hold -title "Peer 4" -e "java p2p init 4 5 8 30" &
#Xterm -hold -title "Peer 5" -e "java p2p init 5 8 9 30" &
#Xterm -hold -title "Peer 8" -e "java p2p init 8 9 14 30" &
#Xterm -hold -title "Peer 9" -e "java p2p init 9 14 19 30" &
#Xterm -hold -title "Peer 14" -e "java p2p init 14 19 2 30" &
#Xterm -hold -title "Peer 19" -e "java p2p init 19 2 4 30" &
cd /Users/weizhouren/COMP9331/p2pNetwork/src
javac p2p.java
ttab -w -t "Peer 2" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 2 4 5 20'
ttab -w -t "Peer 4" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 4 5 8 20'
ttab -w -t "Peer 5" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 5 8 9 20'
ttab -w -t "Peer 8" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 8 9 14 20'
ttab -w -t "Peer 9" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 9 14 19 20'
ttab -w -t "Peer 14" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 14 19 2 20'
ttab -w -t "Peer 19" 'cd /Users/weizhouren/COMP9331/p2pNetwork/src; java p2p init 19 2 4 20'
