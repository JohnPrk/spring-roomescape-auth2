package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Reservations;
import roomescape.dto.ReservationResponses;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.ForbiddenException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final String PAST_RESERVATION_CANCEL_REJECTED = "이미 지난 예약은 취소할 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약이 아닙니다.";

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponses getReservationPage(int page, int size) {
        List<Reservation> reservations = reservationRepository.findAll(page * size, size);
        long totalCount = reservationRepository.count();
        return ReservationResponses.from(reservations, totalCount, page, size);
    }

    public ReservationResponses getMyReservations(Member member) {
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());
        return ReservationResponses.from(reservations, reservations.size(), 0, reservations.size());
    }

    public boolean hasReservationsByTimeId(Long timeId) {
        return reservationRepository.existsByTimeId(timeId);
    }

    public Reservation findMyReservation(Long id, Member member) {
        Reservation reservation = reservationRepository.findById(id);
        if (!reservation.isOwnedBy(member)) {
            throw new ForbiddenException(NOT_OWNER);
        }
        return reservation;
    }

    @Transactional
    public Reservation addReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateReservation(Reservation reservation) {
        return reservationRepository.update(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelMyReservation(Long id, Member member) {
        Reservation reservation = findMyReservation(id, member);

        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_CANCEL_REJECTED);
        }

        reservationRepository.deleteById(id);
    }

    public boolean hasReservationsByThemeId(Long themeId) {
        return reservationRepository.existsByThemeId(themeId);
    }

    public Reservations findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId);
    }
}
