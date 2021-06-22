*COMPILE*

- under src/:

* compile manually:
1- mkdir build
2- javac files/*.java messages/*.java peer/*.java tasks/*.java client/*.java jobs/*.java -d build

* compile with .sh script:
1- sh ../scripts/compile.sh

--------

*EXECUTE PEER*

- under src/build/:

* run manually:
1- start rmiregistry in background
  rmiregistry &
2- start peer
  java peer.Peer <PROTOCOL_VERSION> <PEER_ID> <SAP> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>

* run with .sh script:
1- start rmiregistry in background
  rmiregistry &
2- start peer
  sh ../../scripts/peer.sh <PROTOCOL_VERSION> <PEER_ID> <SAP> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>

PROTOCOL_VERSION:
- 1.0 for vanilla version
- 2.0 for enhanced version

PEER_ID: integer from 0 to inf

SAP (Service Access Point, used to bind name on RMI registry):
- peer<PEER_ID> is used to allow for better understanding

Multicast IPs:
- These must be different from each one, starting at 224.0.0.1

Multicast Ports:
- Ports used for multicast channels, they should also be different from one another

--------

*EXECUTE CLIENT*

- under src/build/:

* run manually:
1- java client.TestApp <SAP> <OPERATION> [<ARGUMENT1> [<ARGUMENT2>] ]

* run with script:
1- sh ../../scripts/test.sh <SAP> <OPERATION> <ARGUMENTS>

SAP (Service Access Point, used to bind name on RMI registry):
- the name used to initiate the peer

OPERATION:
- BACKUP (takes a path and a desired replication degree)
- RESTORE (takes a path)
- DELETE (takes a path)
- RECLAIM (takes a number in KB)
- STATE (takes no arguments)
