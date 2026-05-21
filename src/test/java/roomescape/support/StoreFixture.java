package roomescape.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.Map;

public final class StoreFixture {

    private static final String DEFAULT_STORE_NAME = "테스트 강남점";

    private StoreFixture() {
    }

    public static Long insertDefaultStore(JdbcTemplate jdbcTemplate) {
        return insertStore(jdbcTemplate, DEFAULT_STORE_NAME);
    }

    public static Long insertStore(JdbcTemplate jdbcTemplate, String name) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("store")
                .usingGeneratedKeyColumns("id");
        return insert.executeAndReturnKey(Map.of("name", name)).longValue();
    }
}
