FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY backend/Server.java /app/Server.java
COPY lib /app/lib

RUN javac -cp "/app/lib/*" Server.java

CMD ["java", "-cp", ".:/app/lib/*", "Server"]