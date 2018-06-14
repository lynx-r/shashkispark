#!/usr/bin/env bash

# Articles
DYNAMO_ENDPOINT=http://localhost:8081 dynamodb-admin &
# Boards
PORT=8002 DYNAMO_ENDPOINT=http://localhost:8083 dynamodb-admin &
PORT=8003 DYNAMO_ENDPOINT=http://localhost:8084 dynamodb-admin &
