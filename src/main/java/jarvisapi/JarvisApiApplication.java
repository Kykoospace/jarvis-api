package jarvisapi;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAsync
@EnableSwagger2
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class JarvisApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(JarvisApiApplication.class, args);
	}
}
