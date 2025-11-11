CONTAINER_NAME="java-discord-music-bot"
IMAGE_NAME="java-discord-music-bot"
IMAGE_VERSION="$1"

if [ -z "$IMAGE_VERSION" ]; then
  IMAGE_VERSION=$(awk '/<groupId>dev.lavalink.youtube<\/groupId>/,/<\/version>/' 'pom.xml' \
                      | grep -m1 "<version>" \
                      | sed -E 's/.*<version>([^<]+)<\/version>.*/\1/')
  echo "Image $IMAGE_NAME:$IMAGE_VERSION already exists"
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
  docker rmi "$IMAGE_NAME:$IMAGE_VERSION"
  docker build . -t $IMAGE_NAME:$IMAGE_VERSION
  docker run -d --restart=unless-stopped -e DISCORD_TOKEN=$DISCORD_TOKEN \
    -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER -e YT_LOGIN=$YT_LOGIN -e YT_PASSWORD=$YT_PASSWORD \
    --name $CONTAINER_NAME $IMAGE_NAME:$IMAGE_VERSION

else
  echo "Image $IMAGE_NAME:$IMAGE_VERSION does not exist"

  docker build . -t $IMAGE_NAME:$IMAGE_VERSION

  dockr stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
  docker run -d --restart=unless-stopped -e DISCORD_TOKEN=$DISCORD_TOKEN \
    -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER -e YT_LOGIN=$YT_LOGIN -e YT_PASSWORD=$YT_PASSWORD \
    --name $CONTAINER_NAME $IMAGE_NAME:$IMAGE_VERSION

  for img in $(docker images $IMAGE_NAME --format '{{.Repository}}:{{.Tag}}'); do
    if [ "$img" != "$IMAGE_NAME:$1" ]; then
      echo "Removing $img"
      docker rmi "$img"
    fi
  done
fi


