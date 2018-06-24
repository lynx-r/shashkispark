#!/usr/bin/env bash

cd /opt/dynamodb_local
echo "Starting dynamodb..."
sls dynamodb start >> ./log/out.log

echo "Dynamodb started"
