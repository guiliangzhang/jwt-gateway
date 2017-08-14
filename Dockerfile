FROM java:8-alpine

COPY ./build/libs/*gateway*jar /home/application.jar

ADD application.yml .

CMD ["java","-jar","/home/application.jar"]
