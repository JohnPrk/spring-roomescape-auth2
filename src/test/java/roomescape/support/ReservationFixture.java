package roomescape.support;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

import java.time.LocalDate;

public final class ReservationFixture {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2026, 8, 5);

    private ReservationFixture() {
    }

    public static Reservation withTime(ReservationTime time) {
        return at(DEFAULT_DATE, time);
    }

    public static Reservation at(LocalDate date, ReservationTime time) {
        return new Reservation(
                MemberFixture.anyUser(),
                date,
                time,
                ThemeFixture.horror(),
                StoreFixture.anyStore()
        );
    }
}
