package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.MemberRepository;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final String UNAUTHENTICATED = "로그인이 필요합니다.";
    private static final String NOT_ADMIN = "관리자 권한이 필요합니다.";

    private final MemberRepository memberRepository;

    public AdminInterceptor(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long memberId = SessionStore.findMemberId(request.getSession(false));
        if (memberId == null) {
            throw new UnauthorizedException(UNAUTHENTICATED);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException(UNAUTHENTICATED));
        if (!member.isAdmin()) {
            throw new ForbiddenException(NOT_ADMIN);
        }
        return true;
    }
}
