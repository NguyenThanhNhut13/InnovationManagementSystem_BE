package vn.edu.iuh.fit.innovationmanagementsystem_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            // Load public key for JWT validation
            java.io.InputStream publicKeyStream = new org.springframework.core.io.ClassPathResource(
                    "keys/public_key.pem").getInputStream();
            byte[] publicKeyBytes = publicKeyStream.readAllBytes();
            String publicKeyPEM = new String(publicKeyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            java.security.spec.X509EncodedKeySpec publicKeySpec = new java.security.spec.X509EncodedKeySpec(
                    java.util.Base64.getDecoder().decode(publicKeyPEM));
            java.security.KeyFactory publicKeyFactory = java.security.KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) publicKeyFactory.generatePublic(publicKeySpec);

            return NimbusJwtDecoder.withPublicKey(publicKey)
                    .signatureAlgorithm(
                            org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                    .build();
        } catch (Exception e) {
            throw new IdInvalidException("Failed to create JWT decoder: " + e.getMessage(), e);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
