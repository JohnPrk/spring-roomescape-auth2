package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.MemberRepository;

import java.io.IOException;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final String UNAUTHENTICATED = "로그인이 필요합니다.";
    private static final String NOT_ADMIN = "관리자 권한이 필요합니다.";

    private final MemberIdResolver memberIdResolver;
    private final MemberRepository memberRepository;

    public AdminInterceptor(MemberIdResolver memberIdResolver, MemberRepository memberRepository) {
        this.memberIdResolver = memberIdResolver;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Long memberId = memberIdResolver.resolve(request);
        if (memberId == null) {
            if (BrowserRequest.isHtmlRequest(request)) {
                response.sendRedirect(BrowserRequest.loginRedirectUrl(request));
                return false;
            }
            throw new UnauthorizedException(UNAUTHENTICATED);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException(UNAUTHENTICATED));
        if (!member.isAdmin()) {
            if (BrowserRequest.isHtmlRequest(request)) {
                response.sendRedirect("/?error=admin_only");
                return false;
            }
            throw new ForbiddenException(NOT_ADMIN);
        }
        return true;
    }
}
