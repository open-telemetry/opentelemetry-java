version=$(.github/scripts/get-version.sh)
if [[ $version =~ ^([0-9]+)\.([0-9]+)\.([0-9]+) ]]; then
  major="${BASH_REMATCH[1]}"
  minor="${BASH_REMATCH[2]}"
  patch="${BASH_REMATCH[3]}"
else
  echo "unexpected version: $version"
  exit 1
fi
if [[ $patch == 0 ]]; then
  if [[ $minor == 0 ]]; then
    prior_major=$((major - 1))
    prior_minor=$(grep -Po "^## Version $prior_major.\K[0-9]+" CHANGELOG.md | head -1)
    prior_version="$prior_major.$prior_minor"
  else
    prior_version="$major.$((minor - 1)).0"
  fi
else
  prior_version="$major.$minor.$((patch - 1))"
fi

echo $prior_version
