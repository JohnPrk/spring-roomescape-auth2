package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class BrowserRequest {

    private static final String HTML_MEDIA_TYPE = "text/html";
    private static final String LOGIN_PATH = "/login";
    private static final String REDIRECT_PARAM = "redirect";

    private BrowserRequest() {
    }

    static boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(HTML_MEDIA_TYPE);
    }

    static String loginRedirectUrl(HttpServletRequest request) {
        String original = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            original = original + "?" + query;
        }
        return LOGIN_PATH + "?" + REDIRECT_PARAM + "=" + URLEncoder.encode(original, StandardCharsets.UTF_8);
    }
}
