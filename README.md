# jwt-gateway

[![Build Status](https://travis-ci.org/jc89/jwt-gateway.svg?branch=master)](https://travis-ci.org/jc89/jwt-gateway)

Zuul jwt entry point

## Usage

```
./gradlew clean build
docker build . -t jwt-gateway
docker run \
    -e JWT_SECRET=18732kybhkbfkhbdfsdfs \
    -e JWT_ISSUER=joel \
    -e FILTER_HEADER=my-header \
    jwt-gateway
```