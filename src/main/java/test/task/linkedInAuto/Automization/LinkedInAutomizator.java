package test.task.linkedInAuto.Automization;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.task.linkedInAuto.interfaces.RecaptchaResolver;

public class LinkedInAutomizator {

	private static final Logger logger = LoggerFactory.getLogger(LinkedInAutomizator.class);
	private ChromeOptions options;
	private WebDriver driver;
	private String webSiteUrl = "https://www.linkedin.com";
	private RecaptchaResolver recaptchaResolver;

	public LinkedInAutomizator(boolean isHeadless) {
		this.options = new ChromeOptions();
		if (isHeadless) {
			options.addArguments("--headless");
		}
		driver = new ChromeDriver(options);
		this.recaptchaResolver= new Solver2Captcha();
	}

	public String getPhotoFromLinkedIn(String username, String password) {
		openLinkedInLoginPage();
		if (loginLinkedIn(username, password)) {
			return getProfilePhotoUrl();
		}
		return "none";
	}

	private String getProfilePhotoUrl() {
		WebElement profilePhotoElement = driver.findElement(By.xpath(
				"//img[@class='feed-identity-module__member-photo EntityPhoto-circle-5 evi-image lazy-image ember-view']"));
		return profilePhotoElement.getAttribute("src");
	}

	private boolean openLinkedInLoginPage() {
		try {
			driver.get(webSiteUrl + "/login");
			logger.info("LinkedIn login page is opened");
			return true;
		} catch (Exception e) {
			logger.error("Error opening LinkedIn login page", e);
			driver.quit();
			return false;
		}
	}

	private boolean loginLinkedIn(String username, String password) {
		WebElement usernameField = driver.findElement(By.id("username"));
		WebElement passwordField = driver.findElement(By.id("password"));
		WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

		enterLoginCredentials(usernameField, passwordField, username, password);
		loginButton.click();

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
		WebElement captcha = findCaptcha(wait);

		if (captcha != null && captcha.isDisplayed()) {
			return handleCaptcha(captcha, wait);
		}

		return isLoggedIn();
	}

	private void enterLoginCredentials(WebElement usernameField, WebElement passwordField, String username,
			String password) {
		usernameField.sendKeys(username);
		passwordField.sendKeys(password);
	}

	private boolean handleCaptcha(WebElement captcha, WebDriverWait wait) {
		logger.info("Captcha detected, attempting to solve...");
		captcha.click();

		if (passCaptcha(driver.getCurrentUrl())) {
			logger.info("Captcha solved successfully, retrying login...");
			WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
			loginButton.click();
			return isLoggedIn();
		} else {
			logger.error("Failed to solve captcha");
			return false;
		}
	}

	private WebElement findCaptcha(WebDriverWait wait) {
		try {
			driver.switchTo().frame("captcha-internal");
			driver.switchTo().frame("arkoseframe");

			WebElement iframe = wait.until(
					ExpectedConditions.presenceOfElementLocated(By.xpath("//iframe[@data-e2e='enforcement-frame']")));
			driver.switchTo().frame(iframe);

			WebElement loadingFrame = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//iframe[@id='fc-iframe-wrap']")));
			driver.switchTo().frame(loadingFrame);

			return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='fc_meta_audio_btn']")));
		} catch (Exception e) {
			logger.error("Error finding captcha", e);
			return null;
		}
	}

	private boolean isLoggedIn() {
		List<WebElement> profilePhotoElements = driver.findElements(By.xpath(
				"//img[@class='feed-identity-module__member-photo EntityPhoto-circle-5 evi-image lazy-image ember-view']"));
		return !profilePhotoElements.isEmpty();
	}

	private boolean passCaptcha(String captchaUrl) {
		try {
			String captchaSolution = recaptchaResolver.solveReCaptcha(captchaUrl, webSiteUrl);
			if (captchaSolution == null || captchaSolution.isEmpty()) {
				logger.error("Failed to solve reCAPTCHA.");
				return false;
			}
			WebElement captchaInput = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@class='sc-ifAKCX kHvpIm']")));
			captchaInput.sendKeys(captchaSolution);

			return true;
		} catch (TimeoutException e) {
			logger.error("Timeout while handling captcha", e);
			return false;
		} catch (Exception e) {
			logger.error("Error handling captcha", e);
			return false;
		}
	}
}
