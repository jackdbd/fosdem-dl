# fosdem-dl (FOSDEM talks downloder)

![CI/CD](https://github.com/jackdbd/fosdem-dl/actions/workflows/ci-cd.yml/badge.svg)

[babashka](https://github.com/babashka/babashka) script to download talks and attachments from the 2003-2020 [FOSDEM](https://fosdem.org/2021/) websites.

![Dependency graph of all the namespaces](./resources/img/namespaces.png)

## Usage

```sh
fosdem-dl talks --help
fosdem-dl tracks --help
```

## Examples

Download all talks from the python track of FOSDEM 2018, in .webm format, with no attachments.

```sh
fosdem-dl talks -y 2018 -t python
```

Download all talks of the web performance track from FOSDEM 2020, in .mp4 format, with all attachments.

```sh
fosdem-dl -y 2020 -t web_performance -f mp4 -a
```

## Tests

Run all tests with either one of the following commands:

```sh
./test_runner.clj
bb test
```

## TODO

> [!CAUTION]
> At the moment this project is in a broken state because it relies on a couple of Babashka pods that are distributed as dynamically linked executables (e.g. you can check with `ldd  /home/jack/.babashka/pods/repository/justone/tabl/0.3.0/linux/x86_64/tabl`), and NixOS cannot execute them.
> Even simply adding those pods in the `:pods` key of the `bb.edn` file breaks the project.
> I would need to patch those dynamically linked executables (e.g. with [patchelf](https://github.com/NixOS/patchelf) ot [AutoPatchelfHook](https://nixos.wiki/wiki/Packaging/Binaries)) or find another solution.

- Fix Babashka pods on NixOS or find alternative solutions.
- Improve error handling.
- Add more tests.
- Save edn file with all conference tracks over the years (to validate the `--track` option for a given `--year`).
- Accept destination directory to let the user decide where to download the files.
- Include the talk's links too? Maybe write them in a text/markdown file?
- Show curl's progress bar. See [here](https://github.com/babashka/babashka.curl/issues/34).
- Make a GUI with [pod-babashka-lanterna](https://github.com/babashka/pod-babashka-lanterna)? Probably not...

## GraalVM native image

See also:

- https://github.com/babashka/babashka/blob/master/script/compile
- https://github.com/clj-easy/graalvm-clojure/blob/master/doc/clojure-graalvm-native-binary.md
- https://github.com/oracle/graal/issues/1265

```sh
native-image -jar target/fosdem-dl-0.1.0-standalone.jar \
  -H:Name=fosdem-dl \
  -H:+ReportExceptionStackTraces \
  -H:ReportAnalysisForbiddenType=java.awt.Toolkit:InHeap,Allocated \
  -H:+StaticExecutableWithDynamicLibC \
  --diagnostics-mode \
  --native-image-info \
  --initialize-at-build-time \
  --report-unsupported-elements-at-runtime \
  --no-fallback \
  --verbose \
  -J-Xmx4500m
```
