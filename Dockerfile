FROM ubuntu:16.04
FROM anapsix/alpine-java:8
MAINTAINER mattgates5@gmail.com
ENV BOOTSTRAP false
ADD target/challenge-1.0-assembly.tar.gz /home/
CMD ["/home/challenge/bin/challenge-executable"]
EXPOSE 7777
EXPOSE 7778
