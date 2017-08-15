FROM java:8-alpine

COPY ./build/libs/*gateway*jar application.jar

COPY application.yml .

CMD ["java","-jar","application.jar"]
