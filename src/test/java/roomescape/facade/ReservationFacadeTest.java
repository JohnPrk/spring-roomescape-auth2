package roomescape.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.ConflictException;
import roomescape.repository.MemberRepository;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.support.MemberFixture;
import roomescape.support.ReservationTimeFixture;
import roomescape.support.StoreFixture;
import roomescape.support.ThemeFixture;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationFacadeTest {

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Long storeId;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.anyUser());
        storeId = StoreFixture.insertDefaultStore(jdbcTemplate);
    }

    @Test
    void 사용중인_시간_삭제시_BusinessRuleViolationException이_발생한다() {
        ReservationTime time = reservationTimeService.addTime(ReservationTimeFixture.tenAM());
        Theme theme = themeService.addTheme(ThemeFixture.horror());
        reservationFacade.addReservation(
                new ReservationRequest(LocalDate.of(2026, 8, 5), time.getId(), theme.getId(), storeId),
                member
        );

        assertThatThrownBy(() -> reservationFacade.deleteTime(time.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void 사용중인_테마_삭제시_BusinessRuleViolationException이_발생한다() {
        ReservationTime time = reservationTimeService.addTime(ReservationTimeFixture.tenAM());
        Theme theme = themeService.addTheme(ThemeFixture.horror());
        reservationFacade.addReservation(
                new ReservationRequest(LocalDate.of(2026, 8, 5), time.getId(), theme.getId(), storeId),
                member
        );

        assertThatThrownBy(() -> reservationFacade.deleteTheme(theme.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void 같은_날짜_시간_테마에_중복_예약시_예외가_발생한다() {
        ReservationTime time = reservationTimeService.addTime(ReservationTimeFixture.tenAM());
        Theme theme = themeService.addTheme(ThemeFixture.horror());
        ReservationRequest request = new ReservationRequest(LocalDate.of(2026, 8, 5), time.getId(), theme.getId(), storeId);

        reservationFacade.addReservation(request, member);

        assertThatThrownBy(() -> reservationFacade.addReservation(request, member))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 지난_날짜로_예약시_예외가_발생한다() {
        ReservationTime time = reservationTimeService.addTime(ReservationTimeFixture.tenAM());
        Theme theme = themeService.addTheme(ThemeFixture.horror());
        LocalDate yesterday = LocalDate.now().minusDays(1);

        ReservationRequest request = new ReservationRequest(yesterday, time.getId(), theme.getId(), storeId);

        assertThatThrownBy(() -> reservationFacade.addReservation(request, member))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
