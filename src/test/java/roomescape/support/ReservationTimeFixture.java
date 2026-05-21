package roomescape.support;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public final class ReservationTimeFixture {

    private ReservationTimeFixture() {
    }

    public static ReservationTime tenAM() {
        return at(LocalTime.of(10, 0));
    }

    public static ReservationTime elevenAM() {
        return at(LocalTime.of(11, 0));
    }

    public static ReservationTime at(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }
}
