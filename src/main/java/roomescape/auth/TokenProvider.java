package roomescape.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class TokenProvider {

    private static final String DELIMITER = ":";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final long ttlMillis;

    public TokenProvider(
            @Value("${auth.token.secret}") String secret,
            @Value("${auth.token.ttl-minutes}") long ttlMinutes
    ) {
        this.secret = secret;
        this.ttlMillis = ttlMinutes * 60_000L;
    }

    public String issue(Long memberId) {
        long expiresAt = System.currentTimeMillis() + ttlMillis;
        String payload = memberId + DELIMITER + expiresAt;
        return payload + DELIMITER + sign(payload);
    }

    public Optional<Long> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split(DELIMITER);
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            long memberId = Long.parseLong(parts[0]);
            long expiresAt = Long.parseLong(parts[1]);
            String expectedSignature = sign(parts[0] + DELIMITER + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                return Optional.empty();
            }
            if (System.currentTimeMillis() >= expiresAt) {
                return Optional.empty();
            }
            return Optional.of(memberId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
