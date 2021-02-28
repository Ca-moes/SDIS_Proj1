mkdir out

javac files/*.java messages/*.java peer/*.java tasks/*.java client/*.java -d .\out\

start cmd /k java -jar McastSnooper.jar 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83


