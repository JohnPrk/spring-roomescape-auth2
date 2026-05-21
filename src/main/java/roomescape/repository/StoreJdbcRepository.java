package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Store;
import roomescape.exception.NotFoundException;

import java.util.List;

@Repository
public class StoreJdbcRepository implements StoreRepository {

    private static final String STORE_NOT_FOUND_FORMAT = "ID %d번 매장을 찾을 수 없습니다.";

    private final JdbcTemplate jdbcTemplate;

    public StoreJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Store> storeRowMapper = (rs, rowNum) -> new Store(
            rs.getLong("id"),
            rs.getString("name")
    );

    @Override
    public Store findById(Long id) {
        String sql = "SELECT id, name FROM store WHERE id = ?";
        List<Store> results = jdbcTemplate.query(sql, storeRowMapper, id);
        return results.stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(STORE_NOT_FOUND_FORMAT.formatted(id)));
    }
}
