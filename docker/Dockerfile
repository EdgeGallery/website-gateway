FROM openjdk:8u201-jdk-alpine
ENV APP_FILE gateway-1.0.0.jar
ENV APP_HOME /usr/app
EXPOSE 8080
COPY target/*.jar $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -cp ./configs:$APP_FILE org.springframework.boot.loader.JarLauncher"]
