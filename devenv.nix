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
    # Set these environment variables to true if you want a statically linked
    # binary when compiling with GraalVM native image.
    BABASHKA_MUSL = "true";
    BABASHKA_STATIC = "true";
    DEBUG_BB_UBERJAR = "true";
    # GH_TOKEN = builtins.readFile "/run/secrets/github-tokens/semantic_release_bot";
    GITHUB_TOKEN = builtins.readFile "/run/secrets/github-tokens/github_packages_push";
    POD_JACKDBD_JSOUP_VERSION = "0.4.0";
    TELEGRAM = builtins.readFile "/run/secrets/telegram/jackdbd_github_bot";
  };

  languages = {
    clojure.enable = true;
    nix.enable = true;
  };

  packages = with pkgs; [
    babashka # Clojure interpreter for scripting
    dive # tool for exploring layers in a container image
    gh # GitHub CLI
    git

    # musl is required by GraalVM native-image when compiling a statically
    # linked executable. I guess I need to include musl if I use graalvm-ce, but
    # not if I use graalvmCEPackages.graalvm-ce-musl.
    graalvmCEPackages.graalvm-ce-musl

    neil
    trivy # vulnerability scanner for containers
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
      dive --version
      dot --version
      gh --version
      git --version
      java --version
      native-image --version
      neil --version
      echo "=== === ==="
    '';
  };
}
