mkdir out

javac files/*.java messages/*.java peer/*.java tasks/*.java -d .\out\

start cmd /k java -jar McastSnooper.jar 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
timeout /t 1

cd out

for /l %%x in (1, 1, 5) do (
   rem timeout /t 1
   start cmd /k java peer.Peer 1.0 peer%%x sap 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83
)

