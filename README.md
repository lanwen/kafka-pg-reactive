# Kafka reactive test

>Experiments around kafka and reactive.

### How to play around 

To build having java 15 `./gradlew build` then `./*/build/libs/*-all.jar` is the final application jar.

If you don't have java, but a lucky owner of a docker environment:

`docker build -t app .`

then 

`docker run -it --rm app -Dproducer.kafka.bootstrap.servers=<...> <producer.jar|consumer.jar>`

### Settings required

#### Producer
- `producer.kafka.bootstrap.servers`

For ssl:
- `consumer.kafka.security.protocol=ssl`
- `consumer.kafka.key.pwd=changeit`

it would expect `./certs/kafka/client.keystore.p12` and `./certs/kafka/client.truststore.jks`

#### Consumer
- `consumer.kafka.bootstrap.servers`
- `consumer.pg.connection` - in form of `r2dbc:postgresql://<usr>:<pwd>@<host>:<port>/<db>[?sslMode=VERIFY_CA&sslRootCert=./certs/pg/ca.pem]`

For ssl:
- `consumer.kafka.security.protocol=ssl`
- `consumer.kafka.key.pwd=changeit`

it would expect `./certs/kafka/client.keystore.p12` and `./certs/kafka/client.truststore.jks`


### Docker compose

Also, can be a one-command-to-rule-them-all: `docker-compose up`

However, it requires an `.env` file with the content: 

```bash
kafka_bootstrap_servers=host:port
kafka_security_protocol=ssl
kafka_key_pwd=changeit
pg_connection_string=postgres://user:pwd@host:port/db?sslMode=VERIFY_CA&sslRootCert=/certs/pg/ca.pem
```

This method assumes that in case of ssl, all certs are placed into `./certs/pg/` and `./certs/kafka/`

## Some things to think about

- Connection pooling in docker container for some reason doesn't work, says only postgresql driver available, however in tests all fine...
- Blockhound and reactor-tools
