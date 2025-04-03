package com.jagrosh.jmusicbot.audio.kolhoz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class YtDlpUpdater {

    public final static Logger LOG = LoggerFactory.getLogger(YtDlpUpdater.class);

    public void update() {
        LOG.info("running yt-dlp update");
        try {
            Process process = Runtime.getRuntime().exec("python3 -m pip install -U yt-dlp --break-system-package");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOG.info(line);
            }
            LOG.info("finished yt-dlp update");
        } catch (IOException e) {
            LOG.error("failed to update yt-dlp", e);
        }
    }

}
