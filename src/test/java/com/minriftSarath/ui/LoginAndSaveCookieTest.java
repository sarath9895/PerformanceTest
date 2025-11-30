package com.minriftSarath.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Execution(ExecutionMode.CONCURRENT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginAndSaveCookieTest {

	@ParameterizedTest
	@CsvFileSource(resources = "/credentials.csv", numLinesToSkip = 0)
	@DisplayName("Parallel Login and Cookie Save")
	public void loginAndSaveCookie(String username, String otp, String password) throws Exception {

		System.out.println("Running for user " + username + " on thread: " + Thread.currentThread().getName());

		try (Playwright playwright = Playwright.create()) {

			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

			BrowserContext context = browser.newContext(new Browser.NewContextOptions()
					.setRecordVideoDir(Paths.get("allure-results/videos")).setRecordVideoSize(1280, 720));

			Page page = context.newPage();

			try {
				Allure.step("Navigating to login page");
				page.navigate("https://minbedrift-frontend-test3-trygno.ujasiri.net/minbedrift/login.html?");

				page.click("(//a[@target='_self' and @role='button'])[1]");

				Allure.step("Entering FN/SSN: " + username);
				page.frameLocator("iframe[title='BankID']").locator("input[inputmode='numeric']").fill(username);
				page.frameLocator("iframe[title='BankID']").locator("//button[@type='submit']").click();

				Allure.step("Entering OTP");
				page.frameLocator("iframe[title='BankID']").locator("//input[@type='password']").fill(otp);
				page.frameLocator("iframe[title='BankID']").locator("//button[@type='submit']").click();

                Allure.step("Entering password");
                page.frameLocator("iframe[title='BankID']")
                .locator("//input[@type='password']")
                .fill(password);
                page.frameLocator("iframe[title='BankID']").locator("//button[@type='submit']").click();

                page.waitForURL("**/velg-bedrift");

				Allure.step("Extracting cookies");

				List<Cookie> allCookies = context.cookies("https://preprod.signicat.com");
				for (Cookie c : allCookies) {
					System.out.println("COOKIE: " + c.name + " domain=" + c.domain + " value=" + c.value);
				}

				String cookieValue = allCookies.stream().filter(c -> c.name.toLowerCase().contains("session"))
						.map(c -> c.value).findFirst()
						.orElseThrow(() -> new RuntimeException("Session cookie not found"));

				String filename = "cookies/" + username + "_cookie_" + UUID.randomUUID() + ".txt";
				try (FileWriter fw = new FileWriter(filename)) {
					fw.write(cookieValue);
				}

				Allure.step("Cookie saved: " + filename);

			} catch (Exception ex) {
				// Screenshot
				byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
				Allure.addAttachment("Failure Screenshot", "image/png", new java.io.ByteArrayInputStream(screenshot),
						"png");

				// HTML source
				Allure.addAttachment("Page HTML", "text/html",
						new java.io.ByteArrayInputStream(page.content().getBytes()), "html");

				throw ex;

			} finally {
				browser.close();
			}
		}
	}
}
