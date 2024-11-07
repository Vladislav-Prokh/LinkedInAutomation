package test.task.linkedInAuto.Automization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import test.task.linkedInAuto.interfaces.RecaptchaResolver;

public class Solver2Captcha implements RecaptchaResolver {

	@Value("${api.key}")
	private String API_KEY;
	private static final String API_URL = "http://2captcha.com/in.php";
	private static final String SOLVE_URL = "http://2captcha.com/res.php";
	
	@Override
	public String solveReCaptcha(String captchaUrl, String webSiteUrl) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			String requestUrl = UriComponentsBuilder.fromHttpUrl(API_URL).queryParam("key", API_KEY)
					.queryParam("method", "userrecaptcha").queryParam("googlekey", webSiteUrl)
					.queryParam("pageurl", captchaUrl).toUriString();

			ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, null, String.class);
			String responseBody = response.getBody();
			System.out.println(responseBody);
			if (responseBody.contains("OK|")) {
				String taskId = responseBody.split("\\|")[1];
				String solution = waitForSolution(taskId);
				return solution;
			} else {
				throw new RuntimeException("Error solving captcha: " + responseBody);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String waitForSolution(String taskId) {
		RestTemplate restTemplate = new RestTemplate();
		String statusUrl = UriComponentsBuilder.fromHttpUrl(SOLVE_URL).queryParam("key", API_KEY)
				.queryParam("action", "get").queryParam("id", taskId).toUriString();

		while (true) {
			ResponseEntity<String> response = restTemplate.getForEntity(statusUrl, String.class);
			String responseBody = response.getBody();
			if (responseBody.contains("OK|")) {
				return responseBody.split("\\|")[1];
			} else if (responseBody.contains("CAPCHA_NOT_READY")) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} else {
				throw new RuntimeException("Error getting captcha solution: " + responseBody);
			}
		}
	}
}
