package com.jagrosh.jmusicbot.cookiesrefresh;

import com.jagrosh.jmusicbot.audio.kolhoz.YtDlpAudioSourceManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class CookiesRefresher {

    boolean headless;
    String browserBinaryPath;
    String ytLogin;
    String ytPassword;
    String cookiesPath;

    public CookiesRefresher(boolean headless, String browserBinaryPath, String ytLogin, String ytPassword, String cookiesPath) {
        this.headless = headless;
        this.browserBinaryPath = browserBinaryPath;
        this.ytLogin = ytLogin;
        this.ytPassword = ytPassword;
        this.cookiesPath = cookiesPath;
    }

    public final static Logger LOG = LoggerFactory.getLogger(CookiesRefresher.class);

    public void overwriteYtCookies() {
        LOG.info("start overwriting yt cookies");
        try {
            var newCookies = getNewCookies();
            LOG.info("cookies retrieved");
            var cookiesString = generateCookiesString(newCookies);
            writeToFile(cookiesPath, cookiesString);
        } catch (Exception e) {
            LOG.error("failed to overwrite yt cookies", e);
        }
        LOG.info("finish overwriting yt cookies");
    }

    Set<Cookie> getNewCookies() {
        WebDriverManager.chromedriver().setup();
        var chromeOptions = new ChromeOptions();

        chromeOptions.setBinary(browserBinaryPath);
        if (headless) {
            chromeOptions.addArguments("--headless");
        }
        chromeOptions.addArguments("--incognito", "--disable-web-security", "--disable-dev-shm-usage", "--no-sandbox",
                "--remote-allow-origins=*", "--allow-running-insecure-content", "--window-size=1920,1080");

        var driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));

        driver.get("https://www.youtube.com");

        driver.findElement(By.cssSelector("a[aria-label=\"Sign in\"]")).click();

        driver.findElement(By.cssSelector("input[type=email]")).sendKeys(ytLogin);
        driver.findElement(By.cssSelector("input[type=email]")).sendKeys(Keys.RETURN);

        driver.findElement(By.cssSelector("input[type=password]")).sendKeys(ytPassword);
        driver.findElement(By.cssSelector("input[type=password]")).sendKeys(Keys.RETURN);

        var newCookies = driver.manage().getCookies();

        driver.quit();

        return newCookies;
    }

    String generateCookiesString(Set<Cookie> cookies) {
        String cookiesString = "# Netscape HTTP Cookie File\n\n";
        for (Cookie c : cookies) {
            var subdomain = "TRUE";
            var isSecure = "TRUE";
            if (c.getSameSite().trim().equalsIgnoreCase("strict")) {
                subdomain = "FALSE";
                isSecure = "FALSE";
            } else if (c.getSameSite().trim().equalsIgnoreCase("lax")) {
                subdomain = "TRUE";
                isSecure = "FALSE";
            }
            var expiry = Optional.ofNullable(c.getExpiry()).map(d -> String.valueOf(d.getTime() / 1000)).orElse("0");
            cookiesString += c.getDomain() + "\t" + subdomain + "\t" + c.getPath() + "\t" + isSecure
                    + "\t" + expiry + "\t" + c.getName() + "\t" + c.getValue() + "\n";
        }
        return cookiesString;
    }

    void writeToFile(String filePath, String content) throws IOException {
        var outputStream = new FileOutputStream(filePath);
        byte[] strToBytes = content.getBytes();

        outputStream.write(strToBytes);
        outputStream.close();
    }
}
