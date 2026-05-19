package roomescape.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ReservationTimeResponses;
import roomescape.dto.TimeWithStatusResponses;
import roomescape.facade.ReservationFacade;
import roomescape.service.ReservationTimeService;

import java.time.LocalDate;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;
    private final ReservationFacade reservationFacade;

    public ReservationTimeController(ReservationTimeService reservationTimeService,
                                     ReservationFacade reservationFacade) {
        this.reservationTimeService = reservationTimeService;
        this.reservationFacade = reservationFacade;
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> search() {
        return ResponseEntity.ok().body(ReservationTimeResponses.from(reservationTimeService.getReservationTimes()));
    }

    @GetMapping("/availability")
    public ResponseEntity<TimeWithStatusResponses> searchAvailableReservationTime(@RequestParam LocalDate date,
                                                                                  @RequestParam Long themeId) {
        return ResponseEntity.ok().body(
                TimeWithStatusResponses.of(reservationFacade.getTimesWithAvailability(date, themeId)));
    }
}
