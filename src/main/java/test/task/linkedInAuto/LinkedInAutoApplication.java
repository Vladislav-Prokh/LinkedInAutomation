package test.task.linkedInAuto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import test.task.linkedInAuto.Automization.LinkedInAutomizator;

@SpringBootApplication
public class LinkedInAutoApplication implements CommandLineRunner {
    
    @Value("${web.driver.path}")
    private String webDriverPath;

    public static void main(String[] args) {
        SpringApplication.run(LinkedInAutoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.setProperty("webdriver.chrome.driver", webDriverPath);
        LinkedInAutomizator automizator = new LinkedInAutomizator(false);
        automizator.getPhotoFromLinkedIn("servervps026@gmail.com", "Z500300z500100");
    }
}