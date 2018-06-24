#!/usr/bin/env bash

echo "Stoping dynamodb..."

lsof -i -P | grep -i 8765 | cut -d\  -f 7 | xargs kill
