package vn.edu.iuh.fit.innovationmanagementsystem_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import vn.edu.iuh.fit.innovationmanagementsystem_be.config.RsaKeyProperties;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
public class InnovationManagementSystemBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(InnovationManagementSystemBeApplication.class, args);
	}

}
