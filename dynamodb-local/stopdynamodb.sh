#!/usr/bin/env bash

ps aux | grep -r "[j]ava.*dynamodb" | awk -F '\ +' '{print $3}' | xargs kill
rm -f /tmp/ShashkiUsers.pwd
