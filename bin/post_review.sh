#!/bin/bash

curl -v -d '@data/review_sample.json' -H "Content-Type: application/json" -X POST http://localhost:8080/review