# Enhanced Authentication System - RSA JWT + Redis Refresh Tokens

## 🔐 **Tổng quan hệ thống bảo mật mới**

Hệ thống đã được nâng cấp với:
- **RSA-256 JWT** cho Access Token (thay vì HMAC-SHA256)
- **Redis-based Refresh Token** với UUID (thay vì JWT refresh token)
- **Token Rotation** cho bảo mật cao
- **Multi-device logout** support

## 🏗️ **Kiến trúc hệ thống**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │    │   Spring Boot   │    │      Redis      │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Store:          │    │ JWT:            │    │ Store:          │
│ - Access Token  │◄──►│ - RSA-256       │    │ - Refresh Token │
│ - Refresh Token │    │ - 1 day TTL     │◄──►│ - UUID format   │
│                 │    │                 │    │ - 7 days TTL    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔑 **Token Flow**

### 1. Đăng nhập
```
POST /api/v1/auth/login
{
    "emailOrPersonnelId": "user@example.com",
    "password": "password123"
}

Response:
{
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
        "accessToken": "eyJhbGciOiJSUzI1NiJ9...",  // RSA-256 JWT
        "refreshToken": "b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c",  // UUID
        "userId": "user-uuid",
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "role": "GIANG_VIEN",
        "departmentName": "Khoa CNTT",
        "expiresIn": 86400  // 1 day in seconds
    },
    "statusCode": 200
}
```

### 2. Làm mới token
```
POST /api/v1/auth/refresh
{
    "refreshToken": "b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c"
}

Response:
{
    "success": true,
    "message": "Làm mới token thành công",
    "data": {
        "accessToken": "eyJhbGciOiJSUzI1NiJ9...",  // New access token
        "refreshToken": "c82d98e3-9b5e-5d6f-ae2c-3e4f5a6b7c8d",  // New refresh token (rotated)
        "expiresIn": 86400  // 1 day in seconds
    },
    "statusCode": 200
}
```

### 3. Đăng xuất
```
POST /api/v1/auth/logout
{
    "refreshToken": "b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c"
}

Response:
{
    "success": true,
    "message": "Đăng xuất thành công",
    "data": "Đăng xuất thành công",
    "statusCode": 200
}
```

### 4. Đăng xuất tất cả thiết bị
```
POST /api/v1/auth/logout-all
Headers: Authorization: Bearer <access-token>

Response:
{
    "success": true,
    "message": "Đăng xuất tất cả thiết bị thành công",
    "data": "Đăng xuất tất cả thiết bị thành công", 
    "statusCode": 200
}
```

## 🛡️ **Tính năng bảo mật**

### 1. **RSA-256 JWT**
- **Public/Private Key**: Tự động tạo RSA 2048-bit key pair
- **Asymmetric Encryption**: Private key để sign, Public key để verify
- **Stateless**: Không cần lưu trữ key secret
- **Scalable**: Dễ scale microservices

### 2. **Redis Refresh Token**
- **UUID Format**: `b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c`
- **TTL Expiration**: Tự động expire sau 7 ngày
- **Instant Revocation**: Xóa ngay lập tức khi logout
- **Session Tracking**: Track tất cả devices của user

### 3. **Token Rotation**
- **One-time Use**: Mỗi refresh token chỉ dùng 1 lần
- **Automatic Rotation**: Tạo refresh token mới mỗi lần refresh
- **Reduced Attack Window**: Giảm thiểu rủi ro nếu token bị lộ

### 4. **Multi-device Support**
- **Device Tracking**: Track tất cả refresh tokens của user
- **Selective Logout**: Logout device cụ thể
- **Logout All**: Logout tất cả devices cùng lúc

## ⚙️ **Cấu hình hệ thống**

### 1. **Redis Configuration**
```properties
# application.properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=123456
spring.data.redis.timeout=2000ms
spring.data.redis.database=0
```

### 2. **JWT Configuration**
```properties
# Access token: 1 day (in milliseconds)
jwt.expiration=86400000

# Refresh token: 7 days (in milliseconds, lưu trong Redis)
jwt.refresh-expiration=604800000
```

**Configuration Best Practices:**
- ✅ **Centralized Configuration**: Tất cả JWT settings trong `application.properties`
- ✅ **No Hardcode**: Loại bỏ hardcode values trong `@Value` annotations
- ✅ **Consistent Units**: Configuration trong milliseconds, API response trong seconds
- ✅ **Environment Specific**: Dễ dàng override cho dev/staging/production
- ✅ **Clean Architecture**: Service layer đọc config và tính toán `expiresIn` động

### 3. **RSA Key Generation**
```java
// RsaKeyConfig.java - Runtime key generation
@Configuration
public class RsaKeyConfig {
    @Bean
    public RsaKeyProperties rsaKeyProperties() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RsaKeyProperties(publicKey, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
}
```

**Key Generation Features:**
- ✅ **Automatic**: Tự động tạo khi khởi động app
- ✅ **Runtime**: Key pair tạo mới mỗi lần restart (Development)
- ✅ **2048-bit RSA**: Enterprise-grade security
- ✅ **Configuration Properties**: Clean integration với Spring Boot
- ⚠️ **Production**: Nên sử dụng key cố định để tránh invalidate tokens khi restart

## 📊 **Redis Data Structure**

### Refresh Token Storage:
```
Key: "refresh_token:b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c"
Value: "user-uuid-here"
TTL: 7 days
```

### User Token Set:
```
Key: "user_refresh_tokens:user-uuid-here"
Value: Set["token1", "token2", "token3", ...]
TTL: 7 days
```

## 🧪 **Testing với Postman**

### 1. **Đăng nhập**
```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
    "emailOrPersonnelId": "user@example.com",
    "password": "password123"
}
```

### 2. **Gọi API với Access Token**
```
GET http://localhost:8080/api/v1/users
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

### 3. **Refresh Token**
```
POST http://localhost:8080/api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c"
}
```

### 4. **Logout**
```
POST http://localhost:8080/api/v1/auth/logout
Content-Type: application/json

{
    "refreshToken": "b91c87f2-8a4d-4c5e-9f1b-2d3e4f5a6b7c"
}
```

### 5. **Logout All Devices**
```
POST http://localhost:8080/api/v1/auth/logout-all
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

## 🔍 **Token Validation Process**

### Access Token (JWT):
1. **Extract** từ Authorization header
2. **Verify signature** với RSA public key
3. **Check expiration** (1 day)
4. **Extract user info** từ claims
5. **Load user details** từ database via CustomUserDetailsService

### Refresh Token (UUID):
1. **Lookup** trong Redis với key `refresh_token:{uuid}`
2. **Get userId** từ Redis value
3. **Check TTL** (7 days auto-expire)
4. **Generate new tokens** nếu valid

## 🚀 **Production Recommendations**

### 1. **RSA Keys**
```java
// Sử dụng key cố định trong production
@Value("${rsa.private-key}")
private String privateKeyPath;

@Value("${rsa.public-key}")  
private String publicKeyPath;
```

### 2. **Redis Security**
```properties
# Sử dụng password và SSL
spring.data.redis.password=strong-password
spring.data.redis.ssl=true
```

### 3. **Token TTL**
```properties
# Current configuration: Balanced security vs UX
jwt.expiration=86400000  # 1 day (current)

# Refresh token
jwt.refresh-expiration=604800000  # 7 days (current)

# Production alternatives:
# jwt.expiration=300000  # 5 minutes (high security)
# jwt.refresh-expiration=2592000000  # 30 days (longer session)
```

### 4. **Monitoring**
- **Redis monitoring**: Track refresh token usage
- **JWT monitoring**: Track access token validation
- **Security alerts**: Unusual token patterns

## 📈 **Performance Benefits**

1. **Reduced Database Load**: Refresh tokens trong Redis, không hit DB
2. **Fast Validation**: RSA verification nhanh hơn database lookup
3. **Horizontal Scaling**: Stateless JWT + Redis cluster
4. **Memory Efficiency**: Redis TTL tự động cleanup

## 🔒 **Security Benefits**

1. **Asymmetric Encryption**: RSA an toàn hơn HMAC
2. **Token Rotation**: Giảm thiểu attack window
3. **Instant Revocation**: Logout ngay lập tức
4. **Session Management**: Full control over user sessions

## 🏗️ **Implementation Architecture**

### 1. **Service Layer Pattern**
```java
// AuthService - Business logic layer
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;  // Dynamic config reading
    
    public LoginResponse login(LoginRequest request) {
        // Authentication, token generation, Redis storage
        // Returns unified LoginResponse with expiresIn calculation
    }
}
```

### 2. **JWT Utility Layer**
```java
// JwtUtil - RSA token operations
@Component
@RequiredArgsConstructor  
public class JwtUtil {
    private final RsaKeyProperties rsaKeyProperties;
    
    // Uses injected RSA keys, no hardcoded values
    public String generateAccessToken(String userId, String email, UserRoleEnum role) {
        return Jwts.builder()
            .signWith(rsaKeyProperties.privateKey(), SignatureAlgorithm.RS256)
            .compact();
    }
    
    public Boolean validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(rsaKeyProperties.publicKey())
            .build();
    }
}
```

### 3. **Redis Service Layer**
```java
// RefreshTokenService - Redis operations
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;  // No hardcoded values
    
    public String createRefreshToken(String userId) {
        String token = UUID.randomUUID().toString();
        // Redis storage with TTL
        redisTemplate.opsForValue().set(
            "refresh_token:" + token, 
            userId, 
            refreshExpiration, 
            TimeUnit.MILLISECONDS
        );
        return token;
    }
}
```

### 4. **Enhanced Security Filter**
```java
// JwtAuthenticationFilter - Request interception
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {
        String jwt = extractTokenFromHeader(request);
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            String userId = jwtUtil.extractUserId(jwt);
            UserDetails userDetails = userDetailsService.loadUserById(userId);
            // Set authentication context
        }
    }
}
```

## 🔄 **Request Flow Diagram**

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant AS as AuthService
    participant JWT as JwtUtil
    participant RTS as RefreshTokenService
    participant R as Redis
    participant DB as Database

    C->>AC: POST /api/v1/auth/login
    AC->>AS: login(LoginRequest)
    AS->>DB: authenticate user
    AS->>JWT: generateAccessToken()
    AS->>RTS: createRefreshToken()
    RTS->>R: store token with TTL
    AS->>AC: LoginResponse
    AC->>C: {accessToken, refreshToken, expiresIn}
    
    Note over C,R: Token Refresh Flow
    C->>AC: POST /api/v1/auth/refresh
    AC->>AS: refreshToken(TokenRefreshRequest)
    AS->>RTS: validateRefreshToken()
    RTS->>R: lookup token
    AS->>RTS: rotateRefreshToken()
    RTS->>R: delete old, create new token
    AS->>JWT: generateAccessToken()
    AS->>AC: TokenRefreshResponse
    AC->>C: {accessToken, refreshToken, expiresIn}
```

## 📋 **Key Features Summary**

### ✅ **Authentication Enhancements**
- **v1 API versioning** với clean endpoint structure
- **Comprehensive DTO pattern** cho request/response  
- **Business logic separation** trong Service layer
- **Enhanced error handling** với meaningful messages
- **Configuration-driven** token expiration times

### ✅ **Security Improvements**  
- **RSA-256 JWT** với runtime key generation
- **Redis refresh token** với automatic TTL cleanup
- **Token rotation** cho enhanced security
- **Multi-device session** management
- **Instant token revocation** capability

### ✅ **Architecture Benefits**
- **Clean separation of concerns** (Controller → Service → Repository)
- **Dependency injection** pattern throughout
- **No hardcoded values** in business logic
- **Testable components** với proper abstractions
- **Production-ready** configuration management

Hệ thống mới này cung cấp **enterprise-grade security** với **clean architecture**, **high performance** và **scalability**! 🎉
