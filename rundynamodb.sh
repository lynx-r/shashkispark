#!/usr/bin/env bash

cd articleservice/aws-resources
sls dynamodb start &
cd ../../boardservice/aws-resources
sls dynamodb start &
cd ../../securityservice/aws-resources
sls dynamodb start &
