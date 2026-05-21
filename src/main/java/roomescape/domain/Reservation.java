package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Store store;

    public Reservation(
            Long id,
            Member member,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Store store
    ) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.store = store;
    }

    public Reservation(
            Member member,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Store store
    ) {
        this(null, member, date, time, theme, store);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Store getStore() {
        return store;
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isOwnedBy(Member other) {
        return member.getId().equals(other.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
