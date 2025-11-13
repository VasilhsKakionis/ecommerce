FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw* ./
COPY pom.xml ./
COPY . .
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/order-1.0.0.jar order-service.jar
EXPOSE 8081 5005
ENV SPRING_PROFILES_ACTIVE=docker
ENV ENABLE_DEBUG=false
ENTRYPOINT ["sh", "-c", "\
if [ \"$ENABLE_DEBUG\" = 'true' ]; then \
  echo '>>> Starting Order Service in DEBUG mode on port 5005'; \
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar order-service.jar; \
else \
  echo '>>> Starting Order Service normally'; \
  java -jar order-service.jar; \
fi"]