FROM adoptopenjdk:15-jdk-hotspot as build
WORKDIR /code
COPY . .
RUN ./gradlew build -x test


FROM adoptopenjdk:15-jre-hotspot
COPY --from=build /code/consumer/build/libs/consumer*-all.jar consumer.jar
COPY --from=build /code/producer/build/libs/producer*-all.jar producer.jar

ENTRYPOINT ["java", "-XshowSettings:vm", "--enable-preview", "-jar"]
CMD ["/producer.jar"]