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
import roomescape.exception.ProblemType;
import roomescape.support.StoreFixture;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ManagerReservationApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFilter managerA;
    private SessionFilter user;
    private SessionFilter admin;
    private SessionFilter customer;
    private Long gangnamId;
    private Long hongdaeId;

    @BeforeEach
    void setUp() {
        gangnamId = StoreFixture.insertStore(jdbcTemplate, "강남점");
        hongdaeId = StoreFixture.insertStore(jdbcTemplate, "홍대점");

        Long managerAId = insertMember("managerA@test.com", "password", "매니저A", "MANAGER");
        insertStoreManager(managerAId, gangnamId);
        insertMember("user@test.com", "password", "유저", "USER");
        insertMember("admin@test.com", "password", "어드민", "ADMIN");
        insertMember("customer@test.com", "password", "고객", "USER");

        managerA = login("managerA@test.com", "password");
        user = login("user@test.com", "password");
        admin = login("admin@test.com", "password");
        customer = login("customer@test.com", "password");
    }

    @Test
    void 매니저는_자기_매장_예약만_조회한다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        createReservation(customer, "2026-08-05", timeId, themeId, gangnamId);
        createReservation(customer, "2026-09-09", timeId, themeId, hongdaeId);

        RestAssured.given().log().all()
                .filter(managerA)
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("totalCount", is(1))
                .body("reservations[0].date", is("2026-08-05"));
    }

    @Test
    void 매니저가_자기_매장_예약을_삭제하면_204() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(customer, "2026-08-05", timeId, themeId, gangnamId);

        RestAssured.given().log().all()
                .filter(managerA)
                .when().delete("/manager/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .filter(managerA)
                .when().get("/manager/reservations")
                .then()
                .statusCode(200)
                .body("totalCount", is(0));
    }

    @Test
    void 매니저가_다른_매장_예약을_삭제하면_403() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(customer, "2026-09-09", timeId, themeId, hongdaeId);

        RestAssured.given().log().all()
                .filter(managerA)
                .when().delete("/manager/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403)
                .body("type", is(ProblemType.FORBIDDEN.uri().toString()));
    }

    @Test
    void 비로그인_사용자가_매니저_라우트에_접근하면_401() {
        RestAssured.given().log().all()
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void USER가_매니저_라우트에_접근하면_403() {
        RestAssured.given().log().all()
                .filter(user)
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(403)
                .body("type", is(ProblemType.FORBIDDEN.uri().toString()));
    }

    @Test
    void ADMIN이_매니저_라우트에_접근하면_403() {
        RestAssured.given().log().all()
                .filter(admin)
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(403)
                .body("type", is(ProblemType.FORBIDDEN.uri().toString()));
    }

    private Integer createReservation(SessionFilter session, String date, Integer timeId, Integer themeId, Long storeId) {
        Map<String, Object> body = new HashMap<>();
        body.put("date", date);
        body.put("timeId", timeId);
        body.put("themeId", themeId);
        body.put("storeId", storeId);
        return RestAssured.given()
                .filter(session)
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Long insertMember(String email, String password, String name, String role) {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                email, password, name, role
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = ?",
                Long.class, email
        );
    }

    private void insertStoreManager(Long memberId, Long storeId) {
        jdbcTemplate.update(
                "INSERT INTO store_manager (member_id, store_id) VALUES (?, ?)",
                memberId, storeId
        );
    }

    private SessionFilter login(String email, String password) {
        SessionFilter filter = new SessionFilter();
        Map<String, String> body = Map.of("email", email, "password", password);
        RestAssured.given()
                .filter(filter)
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().statusCode(200);
        return filter;
    }

    private Integer createTime(String startAt) {
        Map<String, String> params = Map.of("startAt", startAt);
        return RestAssured.given()
                .filter(admin)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTheme(String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = Map.of(
                "name", name,
                "description", description,
                "thumbnailImageUrl", thumbnailImageUrl
        );
        return RestAssured.given()
                .filter(admin)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }
}
