# === STAGE 1 ================================================================ #
# Build the uberjar
# ============================================================================ #
# Use this command to list all available tags for the container image:
# skopeo list-tags docker://docker.io/clojure
FROM docker.io/library/clojure:temurin-23-tools-deps-1.12.0.1479-bullseye-slim AS bb-uberjar-builder

ARG APP_DIR=/usr/src/app
RUN if [ -z "${APP_DIR}" ] ; then echo "APP_DIR not set!" ; exit 1; fi

ARG APP_NAME
RUN if [ -z "${APP_NAME}" ] ; then echo "APP_NAME not set!" ; exit 1; fi

ARG APP_VERSION
RUN if [ -z "${APP_VERSION}" ] ; then echo "APP_VERSION not set!" ; exit 1; fi

ARG CREATED_DATE
RUN if [ -z "${CREATED_DATE}" ] ; then echo "CREATED_DATE not set!" ; exit 1; fi

# https://github.com/babashka/babashka/releases
ARG BB_VERSION="1.4.192"

# Case A: pod version on pod registry
# Babashka will download the pod here when running any Babashka task, like
# `bb run build:bb-uber`, because in the bb.edn file we specified a `:version`
# of this pod which is available on pod registry.
# ARG JSOUP_POD_VERSION
# RUN if [ -z "${JSOUP_POD_VERSION}" ] ; then echo "JSOUP_POD_VERSION not set!" ; exit 1; fi
# Case B: pod version NOT on pod registry
# If you need to use a version of pod-jackdbd-jsoup which is not available on
# pod registry, you will need to use `:path` in the bb.edn file, download the
# pod it manually, and place it where bb.edn declares it.
# (e.g. {:pods {com.github.jackdbd/jsoup {:path "/usr/src/app/pods/jsoup"}}}).

# Use a single RUN instruction to create just one layer in the container image.
RUN apt update && \
    apt install wget && \
    wget --directory-prefix /tmp "https://github.com/babashka/babashka/releases/download/v${BB_VERSION}/babashka-${BB_VERSION}-linux-amd64-static.tar.gz" && \
    tar xf "/tmp/babashka-${BB_VERSION}-linux-amd64-static.tar.gz" --directory=/tmp && \
    mv /tmp/bb /usr/local/bin/bb && \
    mkdir -p ${APP_DIR}

WORKDIR ${APP_DIR}

# I think that resources (i.e. assets) and source code change frequently, while
# build scripts and dependencies change less frequently. That's why I decided
# to define the docker layers in this order.
COPY deps.edn ${APP_DIR}/
COPY bb.edn ${APP_DIR}/
COPY build.clj ${APP_DIR}/
COPY bb ${APP_DIR}/bb
# COPY ${JSOUP_POD_PATH} ${APP_DIR}/${JSOUP_POD_PATH}
COPY src ${APP_DIR}/src

RUN bb run build:bb-uber && \
    mv target/${APP_NAME}-${APP_VERSION}.jar "${APP_NAME}.jar"

# === STAGE 2 ================================================================ #
# Run the uberjar
# ============================================================================ #
# Use this command to list all available tags for the container image:
# skopeo list-tags docker://docker.io/babashka/babashka
FROM docker.io/babashka/babashka:1.4.193-SNAPSHOT AS bb-uberjar-runner

ARG APP_DIR=/usr/src/app
RUN if [ -z "${APP_DIR}" ] ; then echo "APP_DIR not set!" ; exit 1; fi

ARG CREATED_DATE
RUN if [ -z "${CREATED_DATE}" ] ; then echo "CREATED_DATE not set!" ; exit 1; fi

ARG JSOUP_POD_VERSION
RUN if [ -z "${JSOUP_POD_VERSION}" ] ; then echo "JSOUP_POD_VERSION not set!" ; exit 1; fi

ARG NON_ROOT_USER=zaraki
RUN if [ -z "${NON_ROOT_USER}" ] ; then echo "NON_ROOT_USER not set!" ; exit 1; fi

RUN groupadd --gid 1234 $NON_ROOT_USER && \
    useradd --uid 1234 --gid 1234 --shell /bin/bash --create-home $NON_ROOT_USER

USER $NON_ROOT_USER
WORKDIR "/home/$NON_ROOT_USER"

COPY --from=bb-uberjar-builder "${APP_DIR}/fosdem-dl.jar" fosdem-dl.jar

# Bake the jsoup pod into the container image.
# NOTE: we could avoid baking the pod into the container image and save ~15 MB,
# but this would mean that Babashka will have to download the pod at runtime
# every single time the container starts.

# Option A: if the builder stage has already downloaded the pod, copied it,
# created a non-root user and gave that user execution permissions on the pod,
# we have nothing to do here.

# Option B: we let Babashka re-download the pod from the pod registry.
RUN bb -e "(require '[babashka.pods :as pods]) \
(pods/load-pod 'com.github.jackdbd/jsoup \"${JSOUP_POD_VERSION}\")"

# This mess is required only when the pod is not available on the pod registry
# and in bb.edn it is declared with :path instead of :version.
# An alternative to this mess would be to set the BABASHKA_PODS_DIR environment
# variable, I think.
# https://github.com/babashka/pods?tab=readme-ov-file#where-does-the-pod-come-from
# RUN bb -e "(require '[babashka.pods :as pods]) \
# (pods/load-pod 'com.github.jackdbd/jsoup \"${JSOUP_POD_VERSION}\")" && \
#     mkdir -p $(dirname $JSOUP_POD_PATH) && \
#     mv $JSOUP_POD_BB_PATH $JSOUP_POD_PATH && \
#     rm -rf "/home/${NON_ROOT_USER}/.babashka"

# https://github.com/opencontainers/image-spec/blob/main/annotations.md
LABEL org.opencontainers.image.created=${CREATED_DATE}
LABEL org.opencontainers.image.description="CLI to download videos and slides from FOSDEM websites"
# https://spdx.github.io/spdx-spec/v2.3/SPDX-license-expressions/
LABEL org.opencontainers.image.licenses="MIT"
# This is required when pushing the container image from a computer to GitHub's Container Registry.
LABEL org.opencontainers.image.source=https://github.com/jackdbd/fosdem-dl
LABEL org.opencontainers.image.title=fosdem-dl
LABEL org.opencontainers.image.url=https://github.com/jackdbd/fosdem-dl

ENTRYPOINT ["bb", "fosdem-dl.jar"]
CMD ["help"]
# CMD ["talks", "--help"]
# CMD ["tracks", "--help"]
