#!/usr/bin/env bash
# Run Spring Boot when Java is not on PATH (common after Homebrew openjdk install).
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
  export PATH="${JAVA_HOME}/bin:${PATH}"
elif [[ -x "/opt/homebrew/opt/openjdk@17/bin/java" ]]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
  export PATH="${JAVA_HOME}/bin:${PATH}"
elif [[ -x "/opt/homebrew/opt/openjdk/bin/java" ]]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
  export PATH="${JAVA_HOME}/bin:${PATH}"
elif [[ -x "/usr/libexec/java_home" ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null)" || true
  if [[ -n "${JAVA_HOME:-}" ]]; then
    export PATH="${JAVA_HOME}/bin:${PATH}"
  fi
fi

if ! command -v java &>/dev/null; then
  echo "No JDK found. Install one of:"
  echo "  brew install openjdk@17"
  echo "Then fix your shell, e.g. in ~/.zshrc:"
  echo '  export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)'
  echo '  export PATH="$JAVA_HOME/bin:$PATH"'
  echo "Or for Homebrew OpenJDK 17 only:"
  echo '  export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"'
  echo '  export PATH="$JAVA_HOME/bin:$PATH"'
  exit 1
fi

echo "Using: $(command -v java) — $(java -version 2>&1 | head -1)"
exec ./mvnw spring-boot:run "$@"
