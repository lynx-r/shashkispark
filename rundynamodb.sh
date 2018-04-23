#!/usr/bin/env bash

cd articleservice
sls dynamodb start &
cd ../boardservice
sls dynamodb start &
cd ../securityservice
sls dynamodb start &
