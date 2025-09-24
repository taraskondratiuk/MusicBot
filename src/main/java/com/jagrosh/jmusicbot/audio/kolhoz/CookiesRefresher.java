package com.jagrosh.jmusicbot.audio.kolhoz;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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

    private CookiesRefresher(boolean headless, String browserBinaryPath, String ytLogin, String ytPassword, String cookiesPath) {
        this.headless = headless;
        this.browserBinaryPath = browserBinaryPath;
        this.ytLogin = ytLogin;
        this.ytPassword = ytPassword;
        this.cookiesPath = cookiesPath;
    }

    public static Optional<CookiesRefresher> init(boolean headless,
                                                  Optional<String> browserBinaryPath,
                                                  Optional<String> ytLogin,
                                                  Optional<String> ytPassword,
                                                  Optional<String> cookiesPath) {
        try {
            var path = browserBinaryPath.filter(v -> !v.isBlank())
                    .orElseThrow(() -> new IllegalArgumentException("browserBinaryPath cannot be empty"));
            var login = ytLogin.filter(v -> !v.isBlank())
                    .orElseThrow(() -> new IllegalArgumentException("ytLogin cannot be empty"));
            var pw = ytPassword.filter(v -> !v.isBlank())
                    .orElseThrow(() -> new IllegalArgumentException("ytPassword cannot be empty"));
            var cookiesPth = cookiesPath.filter(v -> !v.isBlank())
                    .orElseThrow(() -> new IllegalArgumentException("cookiesPath cannot be empty"));
            return Optional.of(new CookiesRefresher(headless, path, login, pw, cookiesPth));

        } catch (Exception e) {
            LOG.error("failed to initialize cookies refresher", e);
            return Optional.empty();
        }
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

    Set<Cookie> getNewCookies() throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        var driver = getRemoteWebDriver();

        driver.get("https://www.youtube.com");

        new WebDriverWait(driver, Duration.ofSeconds(180))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[aria-label=\"Sign in\"]"))).click();
        LOG.info("sing in btn pressed");

        var loginEl = new WebDriverWait(driver, Duration.ofSeconds(180))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type=email]")));
        LOG.info("login field loaded");
        loginEl.sendKeys(ytLogin);
        loginEl.sendKeys(Keys.RETURN);


        var pwEl = new WebDriverWait(driver, Duration.ofSeconds(180))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type=password]")));
        LOG.info("pw field loaded");
        pwEl.sendKeys(ytPassword);
        pwEl.sendKeys(Keys.RETURN);

        Thread.sleep(60 * 1000);

        var newCookies = driver.manage().getCookies();

        driver.quit();
        LOG.info("cookies retrieved: " + newCookies.stream().map(Cookie::toString).reduce("", (a, b) -> a + "\n" + b));

        return newCookies;
    }

    private @NotNull RemoteWebDriver getRemoteWebDriver() {
        WebDriverManager.firefoxdriver().setup();
        var firefoxOptions = new FirefoxOptions();

        firefoxOptions.setBinary(browserBinaryPath);
        if (headless) {
            firefoxOptions.addArguments("-headless");
        }
        firefoxOptions.addArguments("-private", "-width=1920", "-height=1080");

        return new FirefoxDriver(firefoxOptions);
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
