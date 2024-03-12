FROM jre-21
LABEL authors="97078"
COPY target/example-web-fat.jar /app.jar
EXPOSE 3020
#java -jar example-fat.jar -active=dev -cluster --vertx-id=example-service
ENTRYPOINT ["java", "-jar", "/app.jar", "-active=dev", "-cluster", "--vertx-id=example-web-service"]