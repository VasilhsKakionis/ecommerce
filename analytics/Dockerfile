FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw* ./
COPY pom.xml ./
COPY . ./
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/analytics-1.0.0.jar analytics-service.jar
EXPOSE 8084 5008
ENV SPRING_PROFILES_ACTIVE=docker
ENV ENABLE_DEBUG=false
ENV SPRING_SERVER_ADDRESS=0.0.0.0
ENTRYPOINT ["sh", "-c", "\
if [ \"$ENABLE_DEBUG\" = 'true' ]; then \
  echo '>>> Starting Analytics Service in DEBUG mode on port 5008'; \
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 -jar analytics-service.jar; \
else \
  echo '>>> Starting Analytics Service normally'; \
  java -jar analytics-service.jar; \
fi"]