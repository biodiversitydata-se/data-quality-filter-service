FROM eclipse-temurin:11-jre

RUN mkdir -p /data/data-quality-filter-service/config

COPY build/libs/data-quality-filter-service-*.war /data/data-quality-filter-service/data-quality-filter-service.war

ENV DOCKERIZE_VERSION=v0.9.3

RUN apt-get update \
    && apt-get install -y wget \
    && wget -O - https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz | tar xzf - -C /usr/local/bin \
    && apt-get autoremove -yqq --purge wget && rm -rf /var/lib/apt/lists/*

CMD java -jar /data/data-quality-filter-service/data-quality-filter-service.war
