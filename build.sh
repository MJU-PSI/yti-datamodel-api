#!/bin/bash

./gradlew build -x test
docker build $* -t yti-datamodel-api .
