package dev.lavalink.youtube.clients.skeleton;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.CannotBeLoaded;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.Web;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class NoUnplayableTracksYoutubeAudioSourceManager extends YoutubeAudioSourceManager {
    public final static Logger LOG = LoggerFactory.getLogger(NoUnplayableTracksYoutubeAudioSourceManager.class);

    public NoUnplayableTracksYoutubeAudioSourceManager() {
    }

    public NoUnplayableTracksYoutubeAudioSourceManager(boolean allowSearch) {
        super(allowSearch);
    }

    public NoUnplayableTracksYoutubeAudioSourceManager(boolean allowSearch, boolean allowDirectVideoIds, boolean allowDirectPlaylistIds) {
        super(allowSearch, allowDirectVideoIds, allowDirectPlaylistIds);
    }

    public NoUnplayableTracksYoutubeAudioSourceManager(@NotNull Client... clients) {
        super(clients);
    }

    public NoUnplayableTracksYoutubeAudioSourceManager(boolean allowSearch, @NotNull Client... clients) {
        super(allowSearch, clients);
    }

    public NoUnplayableTracksYoutubeAudioSourceManager(boolean allowSearch, boolean allowDirectVideoIds, boolean allowDirectPlaylistIds, @NotNull Client... clients) {
        super(allowSearch, allowDirectVideoIds, allowDirectPlaylistIds, clients);
    }

    public NoUnplayableTracksYoutubeAudioSourceManager(YoutubeSourceOptions options, @NotNull Client... clients) {
        super(options, clients);
    }

    @Override
    public @Nullable AudioItem loadItem(@NotNull AudioPlayerManager manager, @NotNull AudioReference reference) {
        try {
            var item = super.loadItem(manager, reference);

            if (item instanceof AudioTrack) {
                checkIfTrackLoadable((AudioTrack) item);
                return item;
            } else if (item instanceof AudioPlaylist) {
                var first = ((AudioPlaylist) item).getTracks().get(0);
                checkIfTrackLoadable(first);
                return item;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.info("failed to load " + reference.identifier + " with YoutubeAudioSourceManager", e);
            return null;
        }
    }

    private void checkIfTrackLoadable(AudioTrack track) {
        try (HttpInterface httpInterface = httpInterfaceManager.getInterface()) {
            Arrays.stream(this.clients)
                    .filter(v -> v instanceof Web)
                    .findFirst()
                    .map(v -> (Web) v)
                    .get()
                    .loadTrackInfoFromInnertube(this, httpInterface, track.getInfo().identifier, null);
        } catch (CannotBeLoaded | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
