package roomescape.auth;

import jakarta.servlet.http.HttpSession;

public final class SessionStore {

    private static final String MEMBER_ID_KEY = "memberId";

    private SessionStore() {
    }

    public static void saveMemberId(HttpSession session, Long memberId) {
        session.setAttribute(MEMBER_ID_KEY, memberId);
    }

    public static Long findMemberId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute(MEMBER_ID_KEY);
    }
}
