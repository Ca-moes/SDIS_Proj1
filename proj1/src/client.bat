cd out

java -version

java client.TestApp peer1 BACKUP "../test50mb" 3
pause
java client.TestApp peer1 RESTORE "../test50mb"
pause