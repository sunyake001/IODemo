#!/bin/bash

for i in {1..30};do
    ./BenchMark -goCount=5 &
done