FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw* ./
COPY pom.xml ./
COPY . .
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/gateway-1.0.0.jar gateway-service.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENV ENABLE_DEBUG=false
ENTRYPOINT ["sh", "-c", "\
if [ \"$ENABLE_DEBUG\" = 'true' ]; then \
  echo '>>> Starting Gateway in DEBUG mode on port 5000'; \
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5000 -jar gateway-service.jar; \
else \
  echo '>>> Starting Gateway normally'; \
  java -jar gateway-service.jar; \
fi"]