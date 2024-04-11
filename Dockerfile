# JMusicBot Dockerfile
FROM maven:3.8.5-openjdk-17 AS builder

ADD . /JMusicBot/
WORKDIR /JMusicBot

# Build JMusicBot
RUN mvn clean
RUN mvn compile
RUN mvn test-compile
RUN mvn test
RUN mvn install

# Build final image using alpine (Distroless) for smaller image size
FROM debian:12.0-slim
COPY --from=builder /JMusicBot/target/JMusicBot-Snapshot-All.jar /JMusicBot/JMusicBot.jar
COPY --from=builder /JMusicBot/config.txt /JMusicBot/config.txt

# Install useful packages
RUN apt-get update
RUN apt-get install -y openjdk-17-jre-headless
RUN apt-get install -y locales && rm -rf /var/lib/apt/lists/* \
	&& localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8

ENV DISCORD_TOKEN ""

ENV DISCORD_BOT_OWNER ""

# Entrypoint of JMusicBot
WORKDIR /JMusicBot
CMD [ "/usr/bin/java", "-Dnogui=true", "-jar", "/JMusicBot/JMusicBot.jar" ]

# docker build . -t java-discord-music-bot
# docker run -d \
#  --restart=unless-stopped \
#  -e DISCORD_TOKEN=$DISCORD_TOKEN \
#  -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER \
#  --name java-discrod-music-bot \
#  java-discord-music-bot
