package roomescape;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.support.ReservationFixture;
import roomescape.support.ReservationTimeFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationsTest {

    private static final ReservationTime TIME_10 = ReservationTimeFixture.tenAM();
    private static final ReservationTime TIME_11 = ReservationTimeFixture.elevenAM();

    @Test
    void 예약된_시간은_occupied_true를_반환한다() {
        Reservation reservation = ReservationFixture.withTime(TIME_10);
        Reservations reservations = new Reservations(List.of(reservation));

        assertThat(reservations.isOccupied(TIME_10)).isTrue();
    }

    @Test
    void 예약되지_않은_시간은_occupied_false를_반환한다() {
        Reservation reservation = ReservationFixture.withTime(TIME_10);
        Reservations reservations = new Reservations(List.of(reservation));

        assertThat(reservations.isOccupied(TIME_11)).isFalse();
    }

    @Test
    void 예약이_없으면_모든_시간은_occupied_false를_반환한다() {
        Reservations reservations = new Reservations(List.of());

        assertThat(reservations.isOccupied(TIME_10)).isFalse();
    }
}
