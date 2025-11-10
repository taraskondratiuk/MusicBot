POM="pom.xml"
LATEST_VERSION=$(curl -s "https://api.github.com/repos/lavalink-devs/youtube-source/releases/latest" \
  | grep -m 1 '"tag_name":' \
  | sed -E 's/.*"([^"]+)".*/\1/')

CURRENT_VERSION=$(awk '/<groupId>dev.lavalink.youtube<\/groupId>/,/<\/version>/' $POM \
                    | grep -m1 "<version>" \
                    | sed -E 's/.*<version>([^<]+)<\/version>.*/\1/')

if [ "$(printf '%s\n' "$LATEST_VERSION" "$CURRENT_VERSION" | sort -V | head -n1)" != "$LATEST_VERSION" ]; then
  echo "bumping yt-source to $LATEST_VERSION"
  awk -v newver="$LATEST_VERSION" '
  /<groupId>dev.lavalink.youtube<\/groupId>/,/<\/version>/ {
    if ($0 ~ /<version>/)
      sub(/<version>[^<]*<\/version>/, "<version>" newver "</version>")
  }
  { print }
  ' "$POM" > "$POM.tmp" && mv "$POM.tmp" "$POM"

  ./redeploy.sh
fi

