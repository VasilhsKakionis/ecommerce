FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw* ./
COPY pom.xml ./
COPY . .
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/event-1.0.0.jar event-service.jar
EXPOSE 8082 5006
ENV SPRING_PROFILES_ACTIVE=docker
ENV ENABLE_DEBUG=false
ENTRYPOINT ["sh", "-c", "\
if [ \"$ENABLE_DEBUG\" = 'true' ]; then \
  echo '>>> Starting Event Service in DEBUG mode on port 5006'; \
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar event-service.jar; \
else \
  echo '>>> Starting Event Service normally'; \
  java -jar event-service.jar; \
fi"]