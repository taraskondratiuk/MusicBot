FROM maven:3.9.9-eclipse-temurin-23-noble

ADD . /JMusicBot/
WORKDIR /JMusicBot

RUN mvn clean
RUN mvn compile
RUN mvn test-compile
RUN mvn test
RUN mvn install

RUN mv target/JMusicBot-Snapshot-All.jar JMusicBot.jar

RUN apt-get update
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
RUN update-locale LANG=en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"

RUN apt-get install python3 -y
RUN apt-get install python3-pip -y
RUN python3 -m pip install -U yt-dlp --break-system-packages
RUN apt-get install ffmpeg -y

RUN DEBIAN_FRONTEND=noninteractive apt install -y software-properties-common
RUN add-apt-repository ppa:mozillateam/ppa -y
RUN apt update
RUN printf 'Package: *\nPin: release o=LP-PPA-mozillateam\nPin-Priority: 1001\n\nPackage: firefox\nPin: version 1:1snap*\nPin-Priority: -1\n' | tee /etc/apt/preferences.d/mozilla-firefox
RUN apt install firefox -y

RUN mkdir "/cookies"
RUN chmod 777 "/cookies"

ENV YT_COOKIES_FILE_PATH "/cookies/cookies.txt"
ENV FIREFOX_BINARY "/usr/bin/firefox"
ENV DISCORD_TOKEN ""
ENV DISCORD_BOT_OWNER ""
ENV YT_LOGIN ""
ENV YT_PASSWORD ""
ENV JMUSICBOT_NOGUI "true"
ENV JMUSICBOT_NOPROMPT "true"
ENV TRACKS_DIR "/tmp"

WORKDIR /JMusicBot
CMD [ "java", "-jar", "/JMusicBot/JMusicBot.jar" ]

# docker build . -t java-discord-music-bot
# docker run -d \
#  --restart=unless-stopped \
#  -e DISCORD_TOKEN=$DISCORD_TOKEN \
#  -e DISCORD_BOT_OWNER=$DISCORD_BOT_OWNER \
#  -e YT_LOGIN=$YT_LOGIN \
#  -e YT_PASSWORD=$YT_PASSWORD \
#  --name java-discord-music-bot \
#  java-discord-music-bot
