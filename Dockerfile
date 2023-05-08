FROM maven:3.8.6-openjdk-11 as builder

RUN mvn install 

FROM folioci/alpine-jre-openjdk11:latest

ENV VERTICLE_FILE mod-feesfines-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

# Copy your fat jar to the container
COPY --from=builder target/${VERTICLE_FILE} ${VERTICLE_HOME}/${VERTICLE_FILE}

# Expose this port locally in the container.
EXPOSE 8081
