FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw* ./
COPY pom.xml ./
COPY . .
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/notification-1.0.0.jar notification-service.jar
EXPOSE 8083 5007
ENV SPRING_PROFILES_ACTIVE=docker
ENV ENABLE_DEBUG=false
ENTRYPOINT ["sh", "-c", "\
if [ \"$ENABLE_DEBUG\" = 'true' ]; then \
  echo '>>> Starting Notification Service in DEBUG mode on port 5007'; \
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007 -jar notification-service.jar; \
else \
  echo '>>> Starting Notification Service normally'; \
  java -jar notification-service.jar; \
fi"]