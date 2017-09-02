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
  * `JWT_SECRET`: The secret to use
  * `JWT_ISSUER`: The issuer
  * `FILTER_HEADER`: The header where to look for the jwt token
  * `TCP_LOGGING_DESTINATION` (Optional): Destination where to send the logs using `LogstashEncoder` 

## Running it
```
java -jar build/libs/jwt-gateway-0.0.1-SNAPSHOT.jar
```

## Dockerizing it

```
docker build . -t jwt-gateway
```

### Running docker with simple logs

```
docker run \
    -e JWT_SECRET=18732kybhkbfkhbdfsdfs \
    -e JWT_ISSUER=joel \
    -e FILTER_HEADER=my-header \
    jwt-gateway
```

### With logs sent to kibana/es

You can send your logs to a tcp destination by setting the `host:port` into the `TCP_LOGGING_DESTINATION` env variable.

Assuming `docker-compose up` for [this](https://github.com/jc89/docker-elk) succeeded

```
export LOGSTASH_NETWORK=$(docker inspect --format='{{range $p, $conf := .NetworkSettings.Networks}}{{$p}}{{end}}' elk_logstash)

docker run --network=$LOGSTASH_NETWORK \
        -e JWT_SECRET=18732kybhkbfkhbdfsdfs \
        -e JWT_ISSUER=joel \
        -e FILTER_HEADER=my-header \
        -e TCP_LOGGING_DESTINATION=logstash:5000 \
        jwt-gateway
```

Now you can go to [kibana](http://localhost:5601) and see some logs