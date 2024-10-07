{pkgs, ...}: {
  enterShell = ''
    versions
    # export JAVA_HOME=$(which java)
    export APP_VERSION=$(app-version)
  '';

  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  env = {
    GH_TOKEN = builtins.readFile "/run/secrets/github-tokens/semantic_release_bot";
    GREET = "devenv";
    POD_JACKDBD_JSOUP_VERSION = "0.4.0-RC.1";
  };

  languages = {
    clojure.enable = true;
    nix.enable = true;
  };

  packages = with pkgs; [
    babashka # Clojure interpreter for scripting
    git

    # musl is required by GraalVM native-image when compiling a statically
    # linked executable. I guess I need to include musl if I use graalvm-ce, but
    # not if I use graalvmCEPackages.graalvm-ce-musl.
    graalvmCEPackages.graalvm-ce-musl

    neil
  ];

  pre-commit.hooks = {
    alejandra.enable = true;
    check-added-large-files.enable = true;
    cljfmt.enable = true;
    # commitizen.enable = true;
    deadnix.enable = true;
    detect-private-keys.enable = true;
    end-of-file-fixer.enable = true;
    # https://github.com/lycheeverse/lychee
    # lychee = {
    #   enable = true;
    #   excludes = [
    #     "src/fosdem_dl/cli.clj"
    #   ];
    # };
    # markdownlint.enable = true;
    # shellcheck.enable = true;
    statix.enable = true;
  };

  scripts = {
    app-version.exec = ''
      bb -e '(-> (slurp "deps.edn") edn/read-string :aliases :neil :project :version)' | tr -d '"'
    '';
    versions.exec = ''
      echo "=== Versions ==="
      bb --version
      dot --version
      git --version
      java --version
      native-image --version
      neil --version
      echo "=== === ==="
    '';
  };
}
