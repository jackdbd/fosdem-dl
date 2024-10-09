fosdem-dl is available as uberjar or Babashka uberjar.

## Uberjar

To run the uberjar you will need to have a JRE installed on your machine.

```sh
java -jar fosdem-dl-0.1.0-RC.1-standalone.jar
```

## Babashka uberjar

To run the [Babashka uberjar](https://book.babashka.org/#_uberjar) you will need to have [Babashka](https://babashka.org/) installed on your machine.

```sh
bb fosdem-dl-0.1.0-RC.1.jar
```

## Container image [TODO]

The CLI can be built as a container image (see `bb.edn`). I just need to push the container image on [Container registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).

## Binary [TODO]

> [!WARNING]
> GraalVM native-image can produce binaries for this CLI, but they immediately crash at runtime. I'm investigating the issue.
