package com.jagrosh.jmusicbot.audio.kolhoz;

import com.jfposton.ytdlp.YtDlp;
import com.jfposton.ytdlp.YtDlpRequest;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class YtDlpAudioSourceManager extends LocalAudioSourceManager {
    public final static Logger LOG = LoggerFactory.getLogger(YtDlpAudioSourceManager.class);

    public static final String SEARCH_PREFIX = "ytsearch:";

    private static final String PROTOCOL_REGEX = "(?:http://|https://|)";
    private static final String DOMAIN_REGEX = "(?:www\\.|m\\.|music\\.|)youtube\\.com";
    private static final String SHORT_DOMAIN_REGEX = "(?:www\\.|)youtu\\.be";
    private static final String VIDEO_ID_REGEX = "(?<v>[a-zA-Z0-9_-]{11})";
    private static final String PLAYLIST_ID_REGEX = "(?<list>(PL|UU)[a-zA-Z0-9_-]+)";

    private static final String TRACKS_DIR = Optional.of(System.getenv("TRACKS_DIR")).get();
    private static final String YT_COOKIES_FILE_PATH = Optional.of(System.getenv("YT_COOKIES_FILE_PATH")).get();
    private static final Pattern directVideoIdPattern = Pattern.compile("^" + VIDEO_ID_REGEX + "$");
    private static final Pattern directPlaylistIdPattern = Pattern.compile("^" + PLAYLIST_ID_REGEX + "$");
    private static final Pattern mainDomainPattern = Pattern.compile("^" + PROTOCOL_REGEX + DOMAIN_REGEX + "/.*");
    private static final Pattern shortHandPattern = Pattern.compile("^" + PROTOCOL_REGEX + "(?:" + DOMAIN_REGEX + "/(?:live|embed|shorts)|" + SHORT_DOMAIN_REGEX + ")/(?<videoId>.*)");

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (reference.identifier.startsWith(SEARCH_PREFIX)
                || mainDomainPattern.matcher(reference.identifier).matches()
                || shortHandPattern.matcher(reference.identifier).matches() //todo maybe add direct video id????
        ) {

            LOG.debug("attempting to load " + reference.identifier + " with YtDlpAudioSourceManager");
            return downloadTrack(reference.identifier)
                    .map(file -> super.loadItem(manager, new AudioReference(file.getAbsolutePath(), file.getName())))
                    .orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public String getSourceName() {
        return "YT_DLP";
    }

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String today() {
        return LocalDateTime.now().format(dateFormat);
    }

    public void cleanOldTracks() {
        if (System.currentTimeMillis() % 10 == 0) {
            var dt = LocalDateTime.now().minusDays(1).format(dateFormat);
            Arrays.stream(new File(TRACKS_DIR).listFiles())
                    .filter(v -> v.isDirectory())
                    .filter(v -> Pattern.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d", v.getName()))
                    .filter(v -> v.getName().compareTo(dt) < 0)
                    .forEach(dir -> {
                        LOG.info("deleting " + dir.getPath());
                        try {
                            FileUtils.deleteDirectory(dir);
                        } catch (IOException e) {
                            LOG.warn("failed to delete dir " + dir.getPath(), e);
                        }
                    });

        }
    }

    public Optional<File> downloadTrack(String req) {
        cleanOldTracks();

        var tmpDirPath = TRACKS_DIR + "/" + today()  + "/" + java.util.UUID.randomUUID().toString();
        YtDlpRequest request = new YtDlpRequest();
        request.setOption("audio-format", "mp3");
        request.setOption("cookies", YT_COOKIES_FILE_PATH);
        request.setOption("embed-thumbnail");
        request.setOption("no-mtime");
        request.setOption("extract-audio");
        request.setOption("default-search", "ytsearch");
        request.setUrl(req.replace(SEARCH_PREFIX, ""));
        request.setDirectory(tmpDirPath);

        try {
            var dir = new File(tmpDirPath);
            dir.mkdirs();
            var resp = YtDlp.execute(request);
            return Optional.of(dir.listFiles()[0]);
        } catch (Exception e) {
            LOG.warn("yt-dlp error on downloading " + req, e);
            return Optional.empty();
        }
    }
}
