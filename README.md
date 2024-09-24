# fosdem-dl (FOSDEM talks downloder)

[babashka](https://github.com/babashka/babashka) script to download talks and attachments from the 2003-2020 [FOSDEM](https://fosdem.org/2021/) websites.

## Usage

### Parameters

```text
  -y, --year YEAR      Select year (default 2020)
  -t, --track TRACK    Select conference track (e.g. web_performance)
  -f, --format FORMAT  Select video format (default webm)
  -a, --attachments    Download each talk's attachments like PDFs and slides (default false)
  -h, --help           Show help
```

### Examples

Download all the 2018 talks from the python track in .webm format; include no attachments.

```sh
./fosdem-dl.sh -y 2018 -t python
```

Download all the 2020 talks from the web performance track in .mp4 format; include all attachments.

```sh
./fosdem-dl.sh -y 2020 -t web_performance -f mp4 -a
```

## Build

In order to use this script you need to generate a GraalVM native image of the [pod-jaydeesimon-jsoup](https://github.com/jaydeesimon/pod-jaydeesimon-jsoup) babashka pod and place it in this project's root. This is necessary because pod-jaydeesimon-jsoup is not yet published on the [Pod registry](https://github.com/babashka/pod-registry).

## Tests

Run all tests with:

```sh
./test-runner.clj
```

## TODO

> [!CAUTION]
> At the moment this project is in a broken state because it relies on a couple of Babashka pods that are distributed as dynamically linked executables (e.g. you can check with `ldd  /home/jack/.babashka/pods/repository/justone/tabl/0.3.0/linux/x86_64/tabl`), and NixOS cannot execute them.
> Even simply adding those pods in the `:pods` key of the `bb.edn` file breaks the project.
> I would need to patch those dynamically linked executables (e.g. with [patchelf](https://github.com/NixOS/patchelf) ot [AutoPatchelfHook](https://nixos.wiki/wiki/Packaging/Binaries)) or find another solution.

- Fix Babashka pods on NixOS or find alternative solutions.
- Improve error handling.
- Add more tests.
- How do I run `.github/workflows/ci.yml` when the pod-jaydeesimon-jsoup binary is not tracked in git?
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
