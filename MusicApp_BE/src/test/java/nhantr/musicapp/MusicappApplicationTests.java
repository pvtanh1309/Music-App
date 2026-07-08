package nhantr.musicapp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test: kiểm tra Spring Application Context khởi động thành công.
 * Bị disable trong CI vì cần kết nối tới PostgreSQL và Redis thật.
 * Chạy thủ công ở local khi cần: ./mvnw test -Dtest=MusicappApplicationTests
 */
@Disabled("Integration test - yêu cầu DB và Redis thật, không chạy trong CI")
@SpringBootTest
class MusicappApplicationTests {

	@Test
	void contextLoads() {
		// Test pass khi Spring Boot khởi động không bị lỗi cấu hình / thiếu bean
	}

}
