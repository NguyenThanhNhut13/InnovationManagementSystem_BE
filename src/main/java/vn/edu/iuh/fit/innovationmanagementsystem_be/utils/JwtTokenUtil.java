package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    private void loadKeys() {
        try {
            // Load private key
            InputStream privateKeyStream = new ClassPathResource(privateKeyPath.replace("classpath:", ""))
                    .getInputStream();
            byte[] privateKeyBytes = privateKeyStream.readAllBytes();
            String privateKeyPEM = new String(privateKeyBytes)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                    java.util.Base64.getDecoder().decode(privateKeyPEM));
            KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = privateKeyFactory.generatePrivate(privateKeySpec);

            // Load public key
            InputStream publicKeyStream = new ClassPathResource(publicKeyPath.replace("classpath:", ""))
                    .getInputStream();
            byte[] publicKeyBytes = publicKeyStream.readAllBytes();
            String publicKeyPEM = new String(publicKeyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                    java.util.Base64.getDecoder().decode(publicKeyPEM));
            KeyFactory publicKeyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = publicKeyFactory.generatePublic(publicKeySpec);

        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi tải khóa RSA: " + e.getMessage());
        }
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration);
    }

    private String generateToken(UserDetails userDetails, long expiration) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expiration);

        Map<String, Object> claims = new HashMap<>();

        // Extract role names as String array instead of GrantedAuthority collection
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
        claims.put("roles", roles);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("innovation-management-system")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(userDetails.getUsername());

        claims.forEach(claimsBuilder::claim);
        JwtClaimsSet claimsSet = claimsBuilder.build();

        return createToken(claimsSet);
    }

    private String createToken(JwtClaimsSet claimsSet) {
        try {
            // Create JWT using Spring Security OAuth2
            JwtEncoderParameters parameters = JwtEncoderParameters.from(claimsSet);

            // Create JWK for the private key
            com.nimbusds.jose.jwk.RSAKey jwk = new com.nimbusds.jose.jwk.RSAKey.Builder(
                    (java.security.interfaces.RSAPublicKey) publicKey).privateKey(privateKey).build();

            JwtEncoder jwtEncoder = new NimbusJwtEncoder((jwkSelector, context) -> List.of(jwk));
            Jwt jwt = jwtEncoder.encode(parameters);
            return jwt.getTokenValue();
        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi tạo token JWT: " + e.getMessage());
        }
    }

    public String extractUsername(String token) {
        try {
            JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((java.security.interfaces.RSAPublicKey) publicKey)
                    .build();
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Instant extractExpiration(String token) {
        try {
            JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((java.security.interfaces.RSAPublicKey) publicKey)
                    .build();
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getExpiresAt();
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean isTokenExpired(String token) {
        Instant expiration = extractExpiration(token);
        return expiration != null && expiration.isBefore(Instant.now());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username != null &&
                    username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
