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

#### Consumer
- `consumer.kafka.bootstrap.servers`
- `consumer.pg.connection` - in form of `r2dbc:postgresql://<usr>:<pwd>@<host>:<port>/<db>`