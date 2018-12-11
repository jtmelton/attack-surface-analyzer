FROM anapsix/alpine-java
VOLUME /tmp
ADD target/attack-surface-analyzer.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
