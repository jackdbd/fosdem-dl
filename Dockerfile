# === STAGE 1 ================================================================ #
# Build the uberjar
# ============================================================================ #
# https://hub.docker.com/_/clojure
FROM clojure:tools-deps-1.11.1.1189-jammy AS uberjar-builder

ARG APP_DIR=/usr/src/app
RUN if [ -z "${APP_DIR}" ] ; then echo "The APP_DIR argument is missing!" ; exit 1; fi

ARG APP_NAME
RUN if [ -z "${APP_NAME}" ] ; then echo "The APP_NAME argument is missing!" ; exit 1; fi

ARG APP_VERSION
RUN if [ -z "${APP_VERSION}" ] ; then echo "The APP_VERSION argument is missing!" ; exit 1; fi

RUN mkdir -p ${APP_DIR}

WORKDIR ${APP_DIR}

# I think that resources (i.e. assets) and source code change frequently, while
# build scripts and dependencies change less frequently. That's why I decided
# to define the docker layers in this order.
COPY build.clj ${APP_DIR}/
COPY deps.edn ${APP_DIR}/
COPY resources ${APP_DIR}/resources
COPY src ${APP_DIR}/src

RUN clojure -T:build uber

# === STAGE 2 ================================================================ #
# Copy the uberjar built at stage 1 and build a GraalVM native image
# ============================================================================ #
# GraalVM Community Edition images based on Oracle Linux 9
# https://github.com/graalvm/container
# ghcr.io/graalvm/$IMAGE_NAME[:][$os_version][-$java_version][-$version][-$build_number]
# Tip: you can use this command to list all tags available for a container image:
# skopeo list-tags docker://ghcr.io/graalvm/native-image

# FROM ghcr.io/graalvm/native-image:ol9-java17-22.3.0-b1 as native-image-builder
FROM ghcr.io/graalvm/native-image:muslib-ol9-java17-22.3.0-b1 as native-image-builder

# Each ARG goes out of scope at the end of the build stage where it was
# defined. That's why we have to repeat it here in this stage.
# To use an arg in multiple stages, EACH STAGE must include the ARG instruction.
# https://docs.docker.com/engine/reference/builder/#scope
# We also need to re-initialize EACH ARG to its default value (if it has one).
ARG APP_DIR=/usr/src/app
ARG APP_NAME
ARG APP_VERSION
ARG JAR_FILE="${APP_DIR}/target/${APP_NAME}-${APP_VERSION}-standalone.jar"

# I think cross-compiling the GraalVM native image requires downloading the C
# library for that OS-architecture and place it here:
# RUN ls /usr/lib64/graalvm/graalvm22-ce-java17/lib/svm/clibraries
# See also how Babashka does it:
# https://github.com/babashka/babashka/blob/master/.github/workflows/build.yml
ARG TARGET="linux-amd64"

# In the GraalVM Community Edition, only the Serial GC is available.
# https://www.graalvm.org/22.0/reference-manual/native-image/MemoryManagement/
# If no maximum Java heap size is specified, a native image that uses the Serial
# GC will set its maximum Java heap size to 80% of the physical memory size.
# https://www.graalvm.org/22.0/reference-manual/native-image/MemoryManagement/#java-heap-size
ARG JVM_MAX_HEAP_SIZE="-Xmx4500m"

WORKDIR /app

COPY --from=uberjar-builder "${JAR_FILE}" "${APP_NAME}.jar"

# useful docs for GraalVM native-image flags
# https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#native-image-options-useful

# RUN native-image -jar "${APP_NAME}.jar" \
#   -H:Name="${APP_NAME}" \
#   -H:+ReportExceptionStackTraces \
#   -H:ReportAnalysisForbiddenType=java.awt.Toolkit:InHeap,Allocated \
#   --diagnostics-mode \
#   --native-image-info \
#   --initialize-at-build-time \
#   --report-unsupported-elements-at-runtime \
#   --no-fallback \
#   --gc=serial \
#   -H:+StaticExecutableWithDynamicLibC \
#   --libc=glibc \
#   "--target=${TARGET}" \
#   --verbose \
#   "-J${JVM_MAX_HEAP_SIZE}"

# use this when using a musl-based image
RUN native-image -jar "${APP_NAME}.jar" \
  -H:Name="${APP_NAME}" \
  -H:+ReportExceptionStackTraces \
  -H:ReportAnalysisForbiddenType=java.awt.Toolkit:InHeap,Allocated \
  --diagnostics-mode \
  --native-image-info \
  --initialize-at-build-time \
  --report-unsupported-elements-at-runtime \
  --no-fallback \
  --gc=serial \
  --static \
  --libc=musl \
  "--target=${TARGET}" \
  --verbose \
  "-J${JVM_MAX_HEAP_SIZE}"

ENTRYPOINT [ "./fosdem-dl" ]
