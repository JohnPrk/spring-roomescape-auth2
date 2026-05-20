package roomescape.support;

public final class TestAuthFixture {

    private static final String TOKEN_SECRET = "spring-roomescape-auth-test-secret";

    private TestAuthFixture() {
    }

    public static String tokenSecret() {
        return TOKEN_SECRET;
    }
}
