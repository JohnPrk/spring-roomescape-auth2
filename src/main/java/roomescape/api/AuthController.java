package roomescape.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.SessionStore;
import roomescape.auth.TokenProvider;
import roomescape.domain.Member;
import roomescape.dto.LoginRequest;
import roomescape.dto.LoginResponse;
import roomescape.service.AuthService;

@RestController
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;

    public AuthController(AuthService authService, TokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login/sessions")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpSession session) {
        Member member = authService.login(request);
        SessionStore.saveMemberId(session, member.getId());
        String accessToken = tokenProvider.issue(member.getId());
        return ResponseEntity.ok(new LoginResponse(accessToken));
    }

    @DeleteMapping("/login/sessions")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
