cd out

java -version

java client.TestApp peer1 BACKUP "../large.jpg" 3
pause
java client.TestApp peer1 RESTORE "../large.jpg"
pause