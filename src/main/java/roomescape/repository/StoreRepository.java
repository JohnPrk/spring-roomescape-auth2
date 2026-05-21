package roomescape.repository;

import roomescape.domain.Store;

import java.util.Optional;

public interface StoreRepository {

    Optional<Store> findById(Long id);
}
