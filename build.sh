#!/bin/bash

# Assert for needed commands
which java
if [[ $? -gt 0 ]]; then
  echo "java executable not found"
fi

which mvn
if [[ $? -gt 0 ]]; then
  echo "maven executable not found"
fi

which docker
if [[ $? -gt 0 ]]; then
  echo "docker executable not found"
fi

mvn clean package
docker build -t mattgates5/challenge:1.0 .

