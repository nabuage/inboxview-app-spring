FROM openjdk:21-jdk

EXPOSE 8080

COPY ./target/inboxview-app-0.0.1-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

ENTRYPOINT [ "java", "-jar", "inboxview-app-0.0.1-SNAPSHOT.jar" ]