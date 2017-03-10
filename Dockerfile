FROM ubuntu:16.04
FROM anapsix/alpine-java:8
MAINTAINER mattgates5@gmail.com
ENV BOOTSTRAP false
ADD target/counterservice-2.0-assembly.tar.gz /opt/
CMD ["/opt/counterservice/bin/counterservice-executable"]
EXPOSE 7777
EXPOSE 7778
