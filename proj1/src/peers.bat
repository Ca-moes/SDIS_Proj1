cd out
start rmiregistry

for /l %%x in (1, 1, 5) do (
   rem timeout /t 1
   start cmd /k java peer.Peer 2.0 %%x peer%%x 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
)