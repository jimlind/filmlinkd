#!/bin/bash
parent_directory="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

docker rm --force filmlinkd &> /dev/null
docker build -t filmlinkd $parent_directory/resources
docker run --hostname filmlinkd.local --name filmlinkd -v $parent_directory/../../:/app -it filmlinkd /bin/zsh