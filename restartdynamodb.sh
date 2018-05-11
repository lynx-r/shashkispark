#!/usr/bin/env bash

echo "Restarting..."
./stopdynamodb.sh > /dev/null
echo "Stopped"
./startdynamodb.sh
echo "Starting..."
