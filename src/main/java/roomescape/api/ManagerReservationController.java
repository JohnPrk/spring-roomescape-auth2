package roomescape.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.domain.Member;
import roomescape.dto.ReservationResponses;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/manager/reservations")
public class ManagerReservationController {

    private final ReservationService reservationService;

    public ManagerReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> search(
            @LoginMember Member manager,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok().body(reservationService.findReservationsForManager(manager, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @LoginMember Member manager) {
        reservationService.deleteReservationByManager(id, manager);
        return ResponseEntity.noContent().build();
    }
}
