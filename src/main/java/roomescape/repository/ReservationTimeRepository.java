package roomescape.repository;

import roomescape.domain.ReservationTime;

import java.util.List;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime time);

    void deleteById(Long id);

    boolean existsById(Long id);

    ReservationTime findById(Long id);
}
