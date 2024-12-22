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
FROM ubuntu:24.04
COPY --from=builder /JMusicBot/target/JMusicBot-Snapshot-All.jar /JMusicBot/JMusicBot.jar
COPY --from=builder /JMusicBot/config.txt /JMusicBot/config.txt

# Install useful packages
RUN apt-get update
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
RUN update-locale LANG=en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"

RUN apt-get install -y openjdk-17-jre-headless

RUN apt-get install python3 -y
RUN apt-get install python3-pip -y
RUN python3 -m pip install -U yt-dlp --break-system-packages
RUN apt-get install ffmpeg -y

RUN mkdir "/cookies"
RUN chmod 777 "/cookies"
ADD cookies.txt /cookies/cookies.txt

ENV YT_COOKIES_FILE_PATH "/cookies/cookies.txt"
ENV DISCORD_TOKEN ""
ENV DISCORD_BOT_OWNER ""
ENV JMUSICBOT_NOGUI "true"
ENV JMUSICBOT_NOPROMPT "true"
ENV TRACKS_DIR "/tmp"

# Entrypoint of JMusicBot
WORKDIR /JMusicBot
CMD [ "/usr/bin/java", "-jar", "/JMusicBot/JMusicBot.jar" ]

# docker build . -t java-discord-music-bot (cookies.txt file with yt cookies need to be in this dir)
# docker run -d \
#  --restart=unless-stopped \
#  -e DISCORD_TOKEN=$DISCORD_TOKEN \
#  -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER \
#  --name java-discrod-music-bot \
#  java-discord-music-bot
