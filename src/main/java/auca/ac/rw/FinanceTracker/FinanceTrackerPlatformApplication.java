package auca.ac.rw.FinanceTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinanceTrackerPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerPlatformApplication.class, args);
	}

}
