fosdem-dl is distributed as an uberjar, a [Babashka uberjar](https://book.babashka.org/#_uberjar) and a container image.

## Babashka uberjar (recommended)

To run the Babashka uberjar you will need to have [Babashka](https://babashka.org/) installed on your machine.

```sh
bb fosdem-dl-0.1.0-RC.1.jar
```

## Uberjar

To run the uberjar you will need to have a JRE installed on your machine.

```sh
java -jar fosdem-dl-0.1.0-RC.1-standalone.jar
```

## Container image

Visit [this page](https://github.com/jackdbd/fosdem-dl/pkgs/container/fosdem-dl) to see the list of container images, then pull the one you want. For example:

```sh
docker pull ghcr.io/jackdbd/fosdem-dl:0.1.0-rc.1
```

## Binary [TODO]

> [!WARNING]
> GraalVM native-image can produce binaries for this CLI, but they immediately crash at runtime. I'm investigating the issue.
