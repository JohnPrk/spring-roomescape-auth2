package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.UnauthorizedException;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String UNAUTHENTICATED = "로그인이 필요합니다.";

    private final MemberIdResolver memberIdResolver;

    public AuthInterceptor(MemberIdResolver memberIdResolver) {
        this.memberIdResolver = memberIdResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (memberIdResolver.resolve(request) == null) {
            if (BrowserRequest.isHtmlRequest(request)) {
                response.sendRedirect(BrowserRequest.loginRedirectUrl(request));
                return false;
            }
            throw new UnauthorizedException(UNAUTHENTICATED);
        }
        return true;
    }
}
