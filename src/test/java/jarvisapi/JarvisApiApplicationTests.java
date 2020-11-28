package jarvisapi;

import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class JarvisApiApplicationTests {

	@ClassRule
	private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.0.22")
			.withDatabaseName("jarvis_db_test")
			.withUsername("jarvis")
			.withPassword("changeit");

	@Test
	void contextLoads() {
		System.out.println("Context loaded");
	}
}
