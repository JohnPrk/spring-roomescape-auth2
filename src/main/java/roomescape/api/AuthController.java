package roomescape.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
        session.setAttribute(AuthService.SESSION_MEMBER_ID, member.getId());
        return ResponseEntity.ok().build();
    }
}
