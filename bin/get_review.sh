#!/bin/bash

ID=${1:-1}

curl -v http://localhost:8080/review/$ID