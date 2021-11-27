# Copyright 2020 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM swr.cn-north-4.myhuaweicloud.com/eg-common/openjdk:8u201-jre-alpine
ENV APP_FILE gateway-1.0.0.jar
ENV APP_HOME /usr/app
ENV UID=166
ENV GID=166
ENV USER_NAME=eguser
ENV GROUP_NAME=eggroup

EXPOSE 8080

# # CREATE APP USER ##
# Set umask
RUN sed -i "s|umask 022|umask 027|g" /etc/profile &&\
    mkdir -p -m 750 $APP_HOME &&\
    mkdir -p -m 550 $APP_HOME/resources &&\
    mkdir -p -m 550 /usr/app/resources/i18n &&\
    apk update &&\
    apk add shadow &&\
    groupadd -r -g $GID $GROUP_NAME &&\
    useradd -r -u $UID -g $GID -d $APP_HOME -s /sbin/nologin -c "Docker image user" $USER_NAME &&\
    chown -R $USER_NAME:$GROUP_NAME $APP_HOME

COPY --chown=$USER_NAME:$GROUP_NAME target/*.jar $APP_HOME/
COPY --chown=$USER_NAME:$GROUP_NAME target/classes/i18n /usr/app/resources/i18n

WORKDIR $APP_HOME

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -cp ./configs:$APP_FILE org.springframework.boot.loader.JarLauncher"]