# syntax=docker/dockerfile:1
FROM node:14-slim
RUN apt-get update

# Install OhMyZsh with Clean template
RUN apt-get install ca-certificates curl --no-install-recommends --yes
RUN bash -c "$(curl -L https://github.com/deluan/zsh-in-docker/releases/download/v1.1.1/zsh-in-docker.sh)" -- -t clean

WORKDIR /app