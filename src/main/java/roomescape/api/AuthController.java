package roomescape.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.SessionStore;
import roomescape.domain.Member;
import roomescape.dto.LoginRequest;
import roomescape.service.AuthService;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/sessions")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request, HttpSession session) {
        Member member = authService.login(request);
        SessionStore.saveMemberId(session, member.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/login/sessions")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
