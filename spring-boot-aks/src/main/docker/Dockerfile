FROM azul/zulu-openjdk-alpine:11.0.7 AS java-build
WORKDIR /jlink
ENV PATH $JAVA_HOME/bin:$PATH
RUN jlink --strip-debug --no-header-files --no-man-pages --compress=2 --module-path $JAVA_HOME \
    --add-modules java.base,java.logging,java.xml,jdk.unsupported,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,jdk.localedata \
    --include-locales=en,ja,*-IN \
    --output jre-min

FROM alpine:3.12.0
COPY --from=java-build /jlink/jre-min /opt/jre-min
ADD spring-boot-sample.jar /tmp/
USER root
ENV PATH /opt/jre-min/bin:$PATH
ENV APP_HOME /opt/spring-boot
RUN apk add shadow \
    && mkdir -p ${APP_HOME}/ \
    && useradd spring -m \
    && chown -R spring:0 ${APP_HOME} \
    && chmod -R g+rw ${APP_HOME} \
    && apk add --no-cache tini \
    && apk add --update --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime  \
    && echo "Asia/Tokyo" > /etc/timezone  \
    && apk del tzdata \
    && mv /tmp/spring-boot-sample.jar ${APP_HOME}/

USER spring
WORKDIR ${APP_HOME}
EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "/opt/spring-boot/spring-boot-sample.jar"]
