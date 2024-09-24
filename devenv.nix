{pkgs, ...}: {
  enterShell = ''
    hello
    versions
  '';

  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  env.GREET = "devenv";

  languages.clojure.enable = true;

  packages = with pkgs; [
    babashka # Clojure interpreter for scripting
    git
    zlib
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
    lychee = {
      enable = true;
      excludes = [
        "src/fosdem_dl/util.cljc"
      ];
    };
    # markdownlint.enable = true;
    shellcheck.enable = true;
    statix.enable = true;
  };

  scripts = {
    hello.exec = ''
      echo hello from $GREET
    '';
    versions.exec = ''
      echo "=== Versions ==="
      bb --version
      git --version
      echo "=== === ==="
    '';
  };
}
