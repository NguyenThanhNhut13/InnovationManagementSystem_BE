package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ai.AiProvider;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ai.OllamaProvider;

@Configuration
public class AiConfig {

    private static final Logger logger = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    @Primary
    public AiProvider aiProvider(OllamaProvider ollamaProvider) {
        logger.info("Configuring AI provider: Ollama (Local)");
        return ollamaProvider;
    }
}
