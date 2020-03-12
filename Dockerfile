# Build Docker Image:
## mvn clean package
## docker build . --tag gcr.io/PROJECT-ID/ambienweb
## docker push gcr.io/PROJECT-ID/ambienweb
## gcloud beta run deploy ambienweb --image gcr.io/PROJECT-ID/ambienweb --allow-unauthenticated --platform managed --project=PROJECT-ID

####

FROM maven:3.5-jdk-8-alpine as builder

COPY target/ambien*.jar /ambienweb.jar

##CMD ["java","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=$PORT","-jar","/ambienweb.jar"]
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ambienweb.jar"]
