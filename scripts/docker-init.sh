#!/bin/sh

if [[ `uname` == 'Darwin' ]]; then
    docker-machine start default
    eval "$(docker-machine env default)"
fi
