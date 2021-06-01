#!/bin/bash
docker rm --force filmlinkd &> /dev/null
docker build -t filmlinkd ./resources
docker run --hostname filmlinkd.local --name filmlinkd -v `pwd`/../../:/app -it filmlinkd /bin/zsh