package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class MemberIdResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public MemberIdResolver(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public Long resolve(HttpServletRequest request) {
        Long memberId = SessionStore.findMemberId(request.getSession(false));
        if (memberId != null) {
            return memberId;
        }
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return tokenProvider.resolve(token).orElse(null);
    }
}
