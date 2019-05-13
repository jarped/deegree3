FROM maven:3.6.1-jdk-8-alpine as builder

# build and extract deegree
RUN mkdir /build && mkdir /target
COPY ./ /build/
RUN cd /build/ && \
  mvn clean install -DskipTests && \
  cp /build/deegree-services/deegree-webservices/target/deegree-webservices-*.war /build/deegree-webservices.war && \
  unzip -o /build/deegree-webservices.war -d /target

# add to image...
FROM tomcat:8.5-jre8-alpine
ENV LANG en_US.UTF-8

# add build info - see hooks/build and http://label-schema.org/
ARG BUILD_DATE
ARG VCS_REF
ARG VCS_URL
LABEL org.label-schema.build-date=$BUILD_DATE \
  org.label-schema.vcs-url=$VCS_URL \
  org.label-schema.vcs-ref=$VCS_REF \
  org.label-schema.schema-version="1.0.0-rc1"

EXPOSE 8080

# set default secrets ( override for production use! )
# consoleSecretKey="deegree"
ENV consoleSecretKey=000001544E797221:564344F65B8F9DDBA6A410E461E7801E10955F56D8679284966F400C68B6CEAB 
ENV apiUser=deegree
ENV apiPass=deegree

RUN mkdir /root/.deegree && \
  rm -r /usr/local/tomcat/webapps/ROOT

COPY --from=builder /target /usr/local/tomcat/webapps/ROOT
#COPY --from=builder /build/deegree-webservices.war /usr/local/tomcat/webapps/

#cmd:
# 1. configure deegreeapi access
# 2. configure console access
# 3. start tomcat
CMD  sed -i '44i <user username="'"$apiUser"'" password="'"$apiPasswd"'" roles="deegree" \/> /' /usr/local/tomcat/conf/tomcat-users.xml \
     && echo $consoleSecretKey >/root/.deegree/console.pw \
     && /usr/local/tomcat/bin/catalina.sh run