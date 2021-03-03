cd out
start rmiregistry

for /l %%x in (1, 1, 5) do (
   rem timeout /t 1
   start cmd /k java peer.Peer 1.0 peer%%x sap 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
)