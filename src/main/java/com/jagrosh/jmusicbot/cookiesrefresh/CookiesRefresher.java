package com.jagrosh.jmusicbot.cookiesrefresh;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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

    private WebElement waitForEl(WebDriver driver, By by) throws InterruptedException {
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(v -> v.findElement(by));
        Thread.sleep(10 * 1000);
        return driver.findElement(by);
    }

    Set<Cookie> getNewCookies() throws InterruptedException {
        WebDriverManager.firefoxdriver().setup();
        var firefoxOptions = new FirefoxOptions();

        firefoxOptions.setBinary(browserBinaryPath);
        if (headless) {
            firefoxOptions.addArguments("-headless");
        }
        firefoxOptions.addArguments("-private", "-width=1920", "-height=1080");

        var driver = new FirefoxDriver(firefoxOptions);

        driver.get("https://www.youtube.com");

        waitForEl(driver, By.cssSelector("a[aria-label=\"Sign in\"]")).click();

        waitForEl(driver, By.cssSelector("input[type=email]")).sendKeys(ytLogin);
        waitForEl(driver, By.cssSelector("input[type=email]")).sendKeys(Keys.RETURN);

        waitForEl(driver, By.cssSelector("input[type=password]")).sendKeys(ytPassword);
        waitForEl(driver, By.cssSelector("input[type=password]")).sendKeys(Keys.RETURN);

        Thread.sleep(20 * 1000);

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
