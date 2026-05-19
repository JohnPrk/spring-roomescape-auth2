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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AvailabilityFlowTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFilter session;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name) VALUES (?, ?, ?)",
                "user@test.com", "password", "사용자"
        );
        session = new SessionFilter();
        RestAssured.given()
                .filter(session)
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user@test.com", "password", "password"))
                .when().post("/login/sessions")
                .then().statusCode(200);
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

        RestAssured.given().log().all()
                .filter(session)
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

    private Integer createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
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
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }
}
