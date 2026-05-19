package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.LoginRequest;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.MemberRepository;

@Service
public class AuthService {

    public static final String SESSION_MEMBER_ID = "memberId";

    private static final String LOGIN_FAILED = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException(LOGIN_FAILED));
        if (!member.getPassword().equals(request.password())) {
            throw new UnauthorizedException(LOGIN_FAILED);
        }
        return member;
    }
}
