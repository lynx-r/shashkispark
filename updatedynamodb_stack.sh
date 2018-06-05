#!/usr/bin/env bash

export SLS_DEBUG=*

cd articleservice/aws-resources
gradle build
sls deploy
cd ../../boardservice/aws-resources
gradle build
sls deploy
cd ../../securityservice/aws-resources
gradle build
sls deploy
