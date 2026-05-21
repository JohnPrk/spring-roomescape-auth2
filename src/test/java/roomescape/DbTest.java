package roomescape;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.support.StoreFixture;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DbTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long storeId;

    @BeforeEach
    void setUp() {
        storeId = StoreFixture.insertDefaultStore(jdbcTemplate);
    }

    @Test
    void 데이터베이스_연동() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION_TIME", null).next()).isTrue();
            assertThat(connection.getMetaData().getTables(null, null, "THEME", null).next()).isTrue();
            assertThat(connection.getMetaData().getTables(null, null, "MEMBER", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void DB_조회_API_전환() {
        SessionFilter adminSession = createAdminAndLogin();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "15:40");
        Long timeId = jdbcTemplate.queryForObject("SELECT id from reservation_time limit 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id from theme limit 1", Long.class);
        Long memberId = jdbcTemplate.queryForObject("SELECT id from member limit 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id, member_id, store_id) VALUES (?, ?, ?, ?, ?)",
                "2023-08-05", timeId, themeId, memberId, storeId
        );

        List<Map> reservations = RestAssured.given().log().all()
                .filter(adminSession)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("reservations", Map.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    void DB_추가_삭제_API_전환() {
        SessionFilter userSession = createMemberAndLogin();
        SessionFilter adminSession = createAdminAndLogin();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        Long timeId = jdbcTemplate.queryForObject("SELECT id from reservation_time limit 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT id from theme limit 1", Long.class);

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        params.put("storeId", storeId);

        RestAssured.given().log().all()
                .filter(userSession)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .filter(adminSession)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    private SessionFilter createMemberAndLogin() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "user@test.com", "password", "사용자", "USER"
        );
        return login("user@test.com", "password");
    }

    private SessionFilter createAdminAndLogin() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "admin@test.com", "password", "어드민", "ADMIN"
        );
        return login("admin@test.com", "password");
    }

    private SessionFilter login(String email, String password) {
        SessionFilter filter = new SessionFilter();
        RestAssured.given()
                .filter(filter)
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when().post("/login/sessions")
                .then().statusCode(200);
        return filter;
    }
}
