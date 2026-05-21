package roomescape.repository;

import roomescape.domain.Manager;

import java.util.Optional;

public interface ManagerRepository {

    Optional<Manager> findByMemberId(Long memberId);
}
