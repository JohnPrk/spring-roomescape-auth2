package roomescape.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.domain.Member;
import roomescape.dto.MemberResponse;

@RestController
@RequestMapping("/members")
public class MemberController {

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@LoginMember Member member) {
        return ResponseEntity.ok(MemberResponse.from(member));
    }
}
