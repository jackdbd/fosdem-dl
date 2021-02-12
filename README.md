# fosdem-dl (FOSDEM talks downloder)

[babashka](https://github.com/babashka/babashka) script to download talks and attachments from the 2003-2020 [FOSDEM](https://fosdem.org/2021/) websites.

## Usage

Here are some examples that show how to use the script.

Show help.

```sh
./fosdem-dl.sh -h
```

Download all the 2018 talks from the python track in .webm format (default) without any attachments.

```sh
./fosdem-dl.sh -y 2018 -t python
```

Download all the 2020 talks from the web performance track in .mp4 format and include all attachments (PDF, slides, etc).

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

- Improve error handling.
- Add more tests.
- How do I run `.github/workflows/ci.yml` when the pod-jaydeesimon-jsoup binary is not tracked in git?
- Save edn file with all conference tracks over the years (to validate the `--track` option for a given `--year`).
- Accept destination directory to let the user decide where to download the files.
- Include the talk's links too? Maybe write them in a text/markdown file?
- Show curl's progress bar. See [here](https://github.com/babashka/babashka.curl/issues/34).
- Make a GUI with [pod-babashka-lanterna](https://github.com/babashka/pod-babashka-lanterna)? Probably not...
