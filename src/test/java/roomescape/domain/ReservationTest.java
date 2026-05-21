package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    private static final Theme ANY_THEME = new Theme(1L, "공포", "설명", "https://example.com/horror.jpg");
    private static final Member ANY_MEMBER = new Member(1L, "user@test.com", "password", "브라운", Role.USER);
    private static final Store ANY_STORE = new Store(1L, "강남점");

    @Test
    void 예약_시점이_현재보다_과거면_isPast가_true() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation(ANY_MEMBER, LocalDate.of(2026, 5, 14), time, ANY_THEME, ANY_STORE);

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    void 예약_시점이_현재보다_미래면_isPast가_false() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(15, 0));
        Reservation reservation = new Reservation(ANY_MEMBER, LocalDate.of(2026, 5, 14), time, ANY_THEME, ANY_STORE);

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void 예약_시점과_현재가_정확히_같으면_isPast가_false() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 30));
        Reservation reservation = new Reservation(ANY_MEMBER, LocalDate.of(2026, 5, 14), time, ANY_THEME, ANY_STORE);

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }
}
