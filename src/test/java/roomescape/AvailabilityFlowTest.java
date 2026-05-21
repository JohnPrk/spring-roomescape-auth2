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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AvailabilityFlowTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFilter userSession;
    private SessionFilter adminSession;
    private Long storeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "user@test.com", "password", "사용자", "USER"
        );
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "admin@test.com", "password", "어드민", "ADMIN"
        );
        storeId = StoreFixture.insertDefaultStore(jdbcTemplate);
        userSession = login("user@test.com", "password");
        adminSession = login("admin@test.com", "password");
    }

    @Test
    void 가용시간_조회_예약생성_재조회시_예약된_시간이_빠진다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer time10 = createTime("10:00");
        Integer time11 = createTime("11:00");
        Integer time12 = createTime("12:00");
        String date = "2026-08-05";

        RestAssured.given().log().all()
                .when().get("/times/availability?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(3))
                .body("times.find { it.id == " + time10 + " }.reserved", is(false))
                .body("times.find { it.id == " + time11 + " }.reserved", is(false))
                .body("times.find { it.id == " + time12 + " }.reserved", is(false));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", date);
        reservation.put("timeId", time11);
        reservation.put("themeId", themeId);
        reservation.put("storeId", storeId);

        RestAssured.given().log().all()
                .filter(userSession)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/availability?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(3))
                .body("times.find { it.id == " + time10 + " }.reserved", is(false))
                .body("times.find { it.id == " + time11 + " }.reserved", is(true))
                .body("times.find { it.id == " + time12 + " }.reserved", is(false));
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

    private Integer createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTheme(String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnailImageUrl", thumbnailImageUrl);

        return RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }
}
