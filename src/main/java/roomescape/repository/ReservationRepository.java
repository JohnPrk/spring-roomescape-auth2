package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.Reservations;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    List<Reservation> findAll(int offset, int limit);

    long count();

    List<Reservation> findAllByStoreId(Long storeId, int offset, int limit);

    long countByStoreId(Long storeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    void deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    Reservation findById(Long id);

    Reservations findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
