package roomescape.repository;

import roomescape.domain.Store;

public interface StoreRepository {

    Store findById(Long id);
}
