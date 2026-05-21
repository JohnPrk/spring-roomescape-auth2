package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Manager;

import java.util.List;
import java.util.Optional;

@Repository
public class ManagerJdbcRepository implements ManagerRepository {

    private final JdbcTemplate jdbcTemplate;

    public ManagerJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Manager> managerRowMapper = (rs, rowNum) -> new Manager(
            rs.getLong("member_id"),
            rs.getLong("store_id")
    );

    @Override
    public Optional<Manager> findByMemberId(Long memberId) {
        String sql = "SELECT member_id, store_id FROM store_manager WHERE member_id = ?";
        List<Manager> results = jdbcTemplate.query(sql, managerRowMapper, memberId);
        return results.stream().findFirst();
    }
}
