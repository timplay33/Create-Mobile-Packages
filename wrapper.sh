#!/usr/bin/env bash
# === Git-Flow Wrapper for Bash (macOS/Linux) ===
# usage:
# ../wrapper.sh 1.20.1 feature start "#issue"

# check if a version param is given (for example 1.20.1)
if [[ -z "$1" ]]; then
  echo "Usage: $0 <version> [git-flow args...]"
  exit 1
fi

VER=$1
shift

# set git flow config
git config gitflow.branch.master mc${VER}/main
git config gitflow.branch.develop mc${VER}/dev
git config gitflow.prefix.feature mc${VER}/feature/
git config gitflow.prefix.hotfix   mc${VER}/hotfix/
git config gitflow.prefix.release  mc${VER}/release/
git config gitflow.prefix.support  mc${VER}/support/

# run git flow
git flow "$@"
