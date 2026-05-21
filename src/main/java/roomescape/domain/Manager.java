package roomescape.domain;

import java.util.Objects;

public class Manager {

    private final Long memberId;
    private final Long storeId;

    public Manager(Long memberId, Long storeId) {
        this.memberId = memberId;
        this.storeId = storeId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public boolean managesStore(Long otherStoreId) {
        return storeId.equals(otherStoreId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Manager manager = (Manager) o;
        return Objects.equals(getMemberId(), manager.getMemberId())
                && Objects.equals(getStoreId(), manager.getStoreId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMemberId(), getStoreId());
    }
}
