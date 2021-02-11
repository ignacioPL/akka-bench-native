#FROM alpine:latest
FROM ubuntu:18.04

#RUN apk --no-cache add ca-certificates
#RUN apk add --no-cache bash

WORKDIR /app/

EXPOSE 9000

COPY target/graalvm-native-image/akka-bench-app .
RUN chmod +x akka-bench-app

CMD ["./akka-bench-app"]