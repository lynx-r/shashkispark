#!/usr/bin/env bash

rkill() {
	ps aux | grep -r "[j]ava.*dynamodb" | awk -F '\ +' '{print $3}' | xargs kill
}

rkill
