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

RUN apt-get install curl -y

RUN DEBIAN_FRONTEND=noninteractive apt install -y software-properties-common
RUN add-apt-repository ppa:mozillateam/ppa -y
RUN apt update
RUN printf 'Package: *\nPin: release o=LP-PPA-mozillateam\nPin-Priority: 1001\n\nPackage: firefox\nPin: version 1:1snap*\nPin-Priority: -1\n' | tee /etc/apt/preferences.d/mozilla-firefox

RUN apt install firefox -y

RUN mkdir "/cookies"
RUN chmod 777 "/cookies"

RUN DEBIAN_FRONTEND=noninteractive apt-get install -y cron
RUN (crontab -l; echo "0 0 * * * python3 -m pip install -U yt-dlp --break-system-packages") | crontab -

ENV YT_COOKIES_FILE_PATH "/cookies/cookies.txt"
ENV FIREFOX_BINARY "/usr/bin/firefox"
ENV DISCORD_TOKEN ""
ENV DISCORD_BOT_OWNER ""
ENV YT_LOGIN ""
ENV YT_PASSWORD ""
ENV JMUSICBOT_NOGUI "true"
ENV JMUSICBOT_NOPROMPT "true"
ENV TIMEOUT_MULTIPLIER "4"
ENV TRACKS_DIR "/tmp"

# Entrypoint of JMusicBot
WORKDIR /JMusicBot
CMD [ "/usr/bin/java", "-jar", "/JMusicBot/JMusicBot.jar" ]

# docker build . -t java-discord-music-bot
# docker run -d \
#  --restart=unless-stopped \
#  -e DISCORD_TOKEN=$DISCORD_TOKEN \
#  -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER \
#  -e YT_LOGIN=$YT_LOGIN \
#  -e YT_PASSWORD=$YT_PASSWORD \
#  --name java-discord-music-bot \
#  java-discord-music-bot
