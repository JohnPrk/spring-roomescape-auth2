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
public class ManagerInterceptor implements HandlerInterceptor {

    private static final String UNAUTHENTICATED = "로그인이 필요합니다.";
    private static final String NOT_MANAGER = "매장 매니저 권한이 필요합니다.";

    private final MemberIdResolver memberIdResolver;
    private final MemberRepository memberRepository;

    public ManagerInterceptor(MemberIdResolver memberIdResolver, MemberRepository memberRepository) {
        this.memberIdResolver = memberIdResolver;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long memberId = memberIdResolver.resolve(request);
        if (memberId == null) {
            throw new UnauthorizedException(UNAUTHENTICATED);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException(UNAUTHENTICATED));
        if (!member.isManager()) {
            throw new ForbiddenException(NOT_MANAGER);
        }
        return true;
    }
}
