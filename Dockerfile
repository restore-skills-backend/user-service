FROM openjdk:17
 ENV SPRING_PROFILES_ACTIVE=dev
 ADD target/user-service.jar user-service.jar
 EXPOSE 8081
ENTRYPOINT ["java","-jar","user-service.jar"]