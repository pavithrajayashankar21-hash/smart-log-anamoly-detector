FROM eclipse-temurin:17

WORKDIR /app

COPY . .

RUN mkdir -p lib

RUN wget -O lib/mysql-connector-j.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.7.0/mysql-connector-j-9.7.0.jar
RUN sed -i 's|jdbc:mysql://localhost:3306|jdbc:mysql://host.docker.internal:3306|' src/com/smartlog/LogAnalyzer.java
RUN javac -cp "lib/*" -d . src/com/smartlog/*.java

CMD ["java", "-cp", ".:lib/*", "com.smartlog.SmartLogDetector"]