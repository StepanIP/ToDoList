FROM openjdk:11
COPY target/todolist-2-RELEASE.jar app.jar
CMD ["java","-jar","app.jar"]