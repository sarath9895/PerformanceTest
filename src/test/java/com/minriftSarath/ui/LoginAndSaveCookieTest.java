package com.minriftSarath.ui;

import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Execution(ExecutionMode.CONCURRENT)
public class LoginAndSaveCookieTest {

    @RepeatedTest(100)
    @DisplayName("Parallel Login and Cookie Save")
    public void loginAndSaveCookie() throws IOException {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setRecordVideoDir(Paths.get("allure-results/videos"))
                            .setRecordVideoSize(1280, 720)
            );

            Page page = context.newPage();

            try {

                Allure.step("Navigating to login page");
                page.navigate("https://test3-tgno.ujasri.net/login");

                Allure.step("Entering credentials");
                page.fill("#username", System.getenv("LOGIN_USERNAME"));
                page.fill("#password", System.getenv("LOGIN_PASSWORD"));
                page.click("#loginButton");

                page.waitForURL("**/dashboard");

                Allure.step("Extracting cookies");
                List<BrowserContext.Cookie> cookies = context.cookies();

                String cookieValue = cookies.stream()
                        .filter(c -> c.name.equals("MinBedriftSession"))
                        .map(c -> c.value)
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException("Cookie not found")
                        );

                String filename = "cookies/cookie_" + UUID.randomUUID() + ".txt";
                FileWriter fw = new FileWriter(filename);
                fw.write(cookieValue);
                fw.close();

                Allure.step("Cookie saved: " + filename);

            } catch (Exception ex) {

                // Attach screenshot
                byte[] screenshot = page.screenshot(
                        new Page.ScreenshotOptions().setFullPage(true)
                );
                Allure.attachment("Failure Screenshot", "image/png", screenshot, ".png");

                // Attach HTML
                Allure.attachment("Page HTML", page.content());

                throw ex;

            } finally {
                browser.close();
            }
        }
    }
}
