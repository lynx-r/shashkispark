#!/usr/bin/env bash

cd /opt/dynamodb_local
echo "Starting dynamodb..."
echo `date -R` >> ./log/out.log
sls dynamodb start >> ./log/out.log

echo "Dynamodb started"
