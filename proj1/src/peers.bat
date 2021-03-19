cd out
start rmiregistry

start cmd /k java peer.Peer 2.0 1 peer1 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
start cmd /k java peer.Peer 1.0 2 peer2 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
start cmd /k java peer.Peer 1.0 3 peer3 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
start cmd /k java peer.Peer 2.0 4 peer4 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
start cmd /k java peer.Peer 2.0 5 peer5 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83