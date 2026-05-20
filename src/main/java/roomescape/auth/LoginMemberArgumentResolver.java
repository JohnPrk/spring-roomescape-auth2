package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.domain.Member;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.MemberRepository;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String UNAUTHENTICATED = "로그인이 필요합니다.";

    private final MemberIdResolver memberIdResolver;
    private final MemberRepository memberRepository;

    public LoginMemberArgumentResolver(MemberIdResolver memberIdResolver, MemberRepository memberRepository) {
        this.memberIdResolver = memberIdResolver;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Long memberId = memberIdResolver.resolve(request);
        if (memberId == null) {
            throw new UnauthorizedException(UNAUTHENTICATED);
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException(UNAUTHENTICATED));
    }
}
