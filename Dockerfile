# Dockerfile for deploying the Spring Boot WAR to Tomcat

# Use a Tomcat 10.1.x image which supports Servlet 6.0 / Jakarta EE 10,
# compatible with Spring Boot 3.x. Ensure JDK version matches your project.
FROM tomcat:10.1-jdk17-temurin

LABEL maintainer="mourad.tlili98@outlook.com"

# Optional: Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Argument to specify the WAR file name.
# This default should match the name of your PLAIN WAR file
# that Gradle produces and that docker-compose.yml passes.
ARG WAR_FILE_NAME=mourad-tlili-interview-demo-0.0.1-SNAPSHOT-plain.war

# Copy your application's PLAIN WAR file into Tomcat's webapps directory as ROOT.war
# This makes your application accessible at http://localhost:8080/
COPY build/libs/${WAR_FILE_NAME} /usr/local/tomcat/webapps/ROOT.war

# EXPOSE 8080 (Usually inherited from base image)
# CMD ["catalina.sh", "run"] (Default for Tomcat image)
