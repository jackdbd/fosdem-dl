# === STAGE 1 ================================================================ #
# Build the uberjar
# ============================================================================ #
# Use this command to list all available tags for the container image:
# skopeo list-tags docker://docker.io/clojure
FROM docker.io/library/clojure:temurin-23-tools-deps-1.12.0.1479-bullseye-slim AS bb-uberjar-builder

ARG APP_DIR=/usr/src/app
RUN if [ -z "${APP_DIR}" ] ; then echo "APP_DIR not set!" ; exit 1; fi

ARG ARTIFACT_NAME
RUN if [ -z "${ARTIFACT_NAME}" ] ; then echo "ARTIFACT_NAME not set!" ; exit 1; fi

ARG ARTIFACT_VERSION
RUN if [ -z "${ARTIFACT_VERSION}" ] ; then echo "ARTIFACT_VERSION not set!" ; exit 1; fi

ARG ARTIFACT_NAME
RUN if [ -z "${ARTIFACT_NAME}" ] ; then echo "ARTIFACT_NAME not set!" ; exit 1; fi

ARG JSOUP_POD_PATH="resources/pod/pod-jackdbd-jsoup"

ARG DEBUG_BB_UBERJAR
RUN echo "DEBUG_BB_UBERJAR is $DEBUG_BB_UBERJAR"

# https://github.com/babashka/babashka/releases
ARG BB_VERSION="1.4.192"

RUN apt update
RUN apt install wget
RUN wget --directory-prefix /tmp "https://github.com/babashka/babashka/releases/download/v${BB_VERSION}/babashka-${BB_VERSION}-linux-amd64-static.tar.gz"
RUN tar xf "/tmp/babashka-${BB_VERSION}-linux-amd64-static.tar.gz" --directory=/tmp
RUN mv /tmp/bb /usr/local/bin/bb

RUN mkdir -p ${APP_DIR}
WORKDIR ${APP_DIR}

# I think that resources (i.e. assets) and source code change frequently, while
# build scripts and dependencies change less frequently. That's why I decided
# to define the docker layers in this order.
COPY deps.edn ${APP_DIR}/
COPY bb.edn ${APP_DIR}/
COPY build.clj ${APP_DIR}/
COPY bb ${APP_DIR}/bb
COPY ${JSOUP_POD_PATH} ${APP_DIR}/${JSOUP_POD_PATH}
COPY src ${APP_DIR}/src

RUN bb run build:bb-uber
RUN mv target/${ARTIFACT_NAME}-${ARTIFACT_VERSION}.jar "${ARTIFACT_NAME}.jar"

# === STAGE 2 ================================================================ #
# Run the uberjar
# ============================================================================ #
# Use this command to list all available tags for the container image:
# skopeo list-tags docker://docker.io/babashka/babashka
FROM docker.io/babashka/babashka:1.4.193-SNAPSHOT AS bb-uberjar-runner

ARG NON_ROOT_USER=zaraki
RUN groupadd --gid 1234 $NON_ROOT_USER && \
    useradd --uid 1234 --gid 1234 --shell /bin/bash --create-home $NON_ROOT_USER

ARG APP_DIR=/usr/src/app
ARG JSOUP_POD_VERSION=0.4.0

USER $NON_ROOT_USER
WORKDIR "/home/$NON_ROOT_USER"

COPY --from=bb-uberjar-builder "${APP_DIR}/fosdem-dl.jar" fosdem-dl.jar

# Babashka downloads pods to $HOME/.babashka/pods/repository, but in my bb.edn
# I declared that this pod is at resources/pod/pod-jackdbd-jsoup
ARG JSOUP_POD_PATH="resources/pod/pod-jackdbd-jsoup"
ARG JSOUP_POD_BB_PATH="/home/${NON_ROOT_USER}/.babashka/pods/repository/com.github.jackdbd/pod.jackdbd.jsoup/$JSOUP_POD_VERSION/linux/x86_64/pod-jackdbd-jsoup"

# We can either copy a local version of the pod...
# COPY --from=bb-uberjar-builder "${APP_DIR}/${JSOUP_POD_PATH}" "${JSOUP_POD_PATH}"
#...or let Babashka download it from the pod registry

# I need to move the pod to the path declared in my bb.edn. If I specify
# :version instead of :path in my bb.edn file, I guess I can leave the pod at
# the location where Babashka downloads it. Another option would be to set the
# BABASHKA_PODS_DIR environment variable I think.
# https://github.com/babashka/pods?tab=readme-ov-file#where-does-the-pod-come-from
# NOTE: I use a single RUN instruction to save one layer in the container image.
# We could download the pod with one RUN instruction and then move it with
# another one, but this would create an additional layer of roughly 26 MB.
# TIP: You can use dive to inspect the layers of the container image.
RUN bb -e "(require '[babashka.pods :as pods]) \
(pods/load-pod 'com.github.jackdbd/jsoup \"${JSOUP_POD_VERSION}\")" && \
    mkdir -p $(dirname $JSOUP_POD_PATH) && \
    mv $JSOUP_POD_BB_PATH $JSOUP_POD_PATH && \
    rm -rf "/home/${NON_ROOT_USER}/.babashka"

ENTRYPOINT ["bb", "fosdem-dl.jar"]
CMD ["help"]
# CMD ["talks", "--help"]
# CMD ["tracks", "--help"]
