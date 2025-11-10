docker stop java-discord-music-bot
docker rm java-discord-music-bot
docker rmi java-discord-music-bot
docker build . -t java-discord-music-bot
docker run -d --restart=unless-stopped -e DISCORD_TOKEN=$DISCORD_TOKEN -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER -e YT_LOGIN=$YT_LOGIN -e YT_PASSWORD=$YT_PASSWORD --name java-discord-music-bot java-discord-music-bot
