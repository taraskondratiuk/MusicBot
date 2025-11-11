CONTAINER_NAME="java-discord-music-bot"
IMAGE_NAME="java-discord-music-bot"
docker build . -t $IMAGE_NAME:$1
docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME
docker run -d --restart=unless-stopped -e DISCORD_TOKEN=$DISCORD_TOKEN -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER -e YT_LOGIN=$YT_LOGIN -e YT_PASSWORD=$YT_PASSWORD --name $CONTAINER_NAME $IMAGE_NAME:$1

for img in $(docker images $IMAGE_NAME --format '{{.Repository}}:{{.Tag}}'); do
  if [ "$img" != "$IMAGE_NAME:$1" ]; then
    echo "Removing $img"
    docker rmi "$img"
  fi
done
