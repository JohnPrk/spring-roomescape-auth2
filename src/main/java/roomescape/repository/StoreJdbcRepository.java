package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Store;

import java.util.List;
import java.util.Optional;

@Repository
public class StoreJdbcRepository implements StoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public StoreJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Store> storeRowMapper = (rs, rowNum) -> new Store(
            rs.getLong("id"),
            rs.getString("name")
    );

    @Override
    public Optional<Store> findById(Long id) {
        String sql = "SELECT id, name FROM store WHERE id = ?";
        List<Store> results = jdbcTemplate.query(sql, storeRowMapper, id);
        return results.stream().findFirst();
    }
}
