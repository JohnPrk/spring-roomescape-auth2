package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.UnauthorizedException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String UNAUTHENTICATED = "로그인이 필요합니다.";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (SessionStore.findMemberId(request.getSession(false)) == null) {
            throw new UnauthorizedException(UNAUTHENTICATED);
        }
        return true;
    }
}
