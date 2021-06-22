FROM openjdk:11

COPY entrypoint_peer.sh /usr/src/
COPY proj1/src /usr/src

RUN apt-get update && apt-get install -y dnsutils net-tools dos2unix

WORKDIR /usr/src

RUN chmod +x entrypoint_peer.sh
RUN dos2unix entrypoint_peer.sh

CMD ./entrypoint_peer.sh
