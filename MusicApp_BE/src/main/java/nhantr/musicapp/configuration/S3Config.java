package nhantr.musicapp.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.s3.region:ap-southeast-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // AWS SDK sẽ tự động lấy thông tin xác thực từ biến môi trường,
        // hoặc từ IAM Role (nếu chạy trên ECS/EC2) cực kỳ thông minh!
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
