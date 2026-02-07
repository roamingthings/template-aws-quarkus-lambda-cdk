#!/bin/sh
set -e
../gradlew -x test clean build && cdk deploy
