FROM maven:3-jdk-11-openj9

ARG SEARCH_SERVER_HOST=http://b-dev1054.pk.de:8983
ARG SEARCH_SERVER_CORE=hsp

ENV SERVICE_NAME hsp-fo-discovery
ENV SERVICE_VERSION 0.2-SNAPSHOT
ENV DISCOVERY_USER hsp-fo-discovery

RUN useradd -mU ${DISCOVERY_USER}

COPY . /opt/${SERVICE_NAME}

RUN chown -R ${DISCOVERY_USER}:${DISCOVERY_USER} /opt/${SERVICE_NAME}

RUN mkdir /data/log && \
    touch /data/log/hsp-fo-discovery.log && \
    chmod 664 /data/log/hsp-fo-discovery.log && \
    chown ${DISCOVERY_USER}:${DISCOVERY_USER} /data/log/hsp-fo-discovery.log

USER ${DISCOVERY_USER}

WORKDIR /opt/${SERVICE_NAME}

RUN mvn package
	
EXPOSE 9295

ENV SOLR_HOST ${SEARCH_SERVER_HOST}
ENV SOLR_CORE ${SEARCH_SERVER_CORE}

CMD ./target/${SERVICE_NAME}-${SERVICE_VERSION}.war
