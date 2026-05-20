package roomescape.auth;

import org.junit.jupiter.api.Test;
import roomescape.support.TestAuthFixture;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TokenProviderTest {

    private static final String SECRET = TestAuthFixture.tokenSecret();

    @Test
    void 발급한_토큰을_resolve하면_같은_memberId가_나온다() {
        TokenProvider provider = new TokenProvider(SECRET, 60);

        String token = provider.issue(42L);

        assertThat(provider.resolve(token)).contains(42L);
    }

    @Test
    void TTL이_0이면_발급_즉시_만료되어_resolve가_빈_값() {
        TokenProvider provider = new TokenProvider(SECRET, 0);

        String token = provider.issue(1L);

        assertThat(provider.resolve(token)).isEmpty();
    }

    @Test
    void 서명이_변조된_토큰은_resolve가_빈_값() {
        TokenProvider provider = new TokenProvider(SECRET, 60);
        String token = provider.issue(1L);

        String[] parts = token.split(":");
        String tampered = parts[0] + ":" + parts[1] + ":" + "0".repeat(parts[2].length());

        assertThat(provider.resolve(tampered)).isEmpty();
    }

    @Test
    void 다른_비밀키로_검증하면_resolve가_빈_값() {
        TokenProvider issuer = new TokenProvider(SECRET, 60);
        TokenProvider verifier = new TokenProvider("another-secret", 60);

        String token = issuer.issue(1L);

        assertThat(verifier.resolve(token)).isEmpty();
    }

    @Test
    void 형식이_잘못된_토큰은_resolve가_빈_값() {
        TokenProvider provider = new TokenProvider(SECRET, 60);

        assertThat(provider.resolve("not-a-token")).isEmpty();
        assertThat(provider.resolve("1:abc:xyz")).isEmpty();
        assertThat(provider.resolve("")).isEmpty();
        assertThat(provider.resolve(null)).isEqualTo(Optional.empty());
    }
}
