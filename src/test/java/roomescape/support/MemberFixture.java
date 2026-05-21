package roomescape.support;

import roomescape.domain.Member;
import roomescape.domain.Role;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member anyUser() {
        return new Member(null, "user@test.com", "password", "브라운", Role.USER);
    }
}
