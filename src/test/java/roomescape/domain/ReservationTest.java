package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.support.ReservationFixture;
import roomescape.support.ReservationTimeFixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    @Test
    void 예약_시점이_현재보다_과거면_isPast가_true() {
        Reservation reservation = ReservationFixture.at(
                LocalDate.of(2026, 5, 14), ReservationTimeFixture.at(LocalTime.of(14, 0)));

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    void 예약_시점이_현재보다_미래면_isPast가_false() {
        Reservation reservation = ReservationFixture.at(
                LocalDate.of(2026, 5, 14), ReservationTimeFixture.at(LocalTime.of(15, 0)));

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void 예약_시점과_현재가_정확히_같으면_isPast가_false() {
        Reservation reservation = ReservationFixture.at(
                LocalDate.of(2026, 5, 14), ReservationTimeFixture.at(LocalTime.of(14, 30)));

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }
}
