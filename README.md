# jwt-gateway

[![Build Status](https://travis-ci.org/jc89/jwt-gateway.svg?branch=master)](https://travis-ci.org/jc89/jwt-gateway)

Zuul jwt entry point

## Build 

```
./gradlew clean build
```

## Configuration

* Change `application.yml` to your needs
* Env variables expected:
  * `JWT_PUBLIC_KEYS_PATHS`: Csv of paths to public keys for verification
  * `JWT_ISSUER`: The issuer
  * `FILTER_HEADER`: The header where to look for the jwt token
  * `TCP_LOGGING_DESTINATION` (Optional): Destination where to send the logs using `LogstashEncoder` 

## Running it
```
java -jar build/libs/jwt-gateway-0.0.1-SNAPSHOT.jar
```
