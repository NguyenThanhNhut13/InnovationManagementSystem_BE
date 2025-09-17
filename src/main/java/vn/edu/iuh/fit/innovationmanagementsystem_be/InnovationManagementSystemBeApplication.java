package vn.edu.iuh.fit.innovationmanagementsystem_be;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "Innovation Management System API", version = "1.0.0", description = "API documentation for Innovation Management System Backend"))
public class InnovationManagementSystemBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(InnovationManagementSystemBeApplication.class, args);
	}

}