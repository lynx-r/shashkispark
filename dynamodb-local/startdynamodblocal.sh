#!/usr/bin/env bash

rm -f ./shashki_online/ShashkiUsers.pwd 
rm -f ./data/shared-local-instance.db

sls dynamodb start $1
