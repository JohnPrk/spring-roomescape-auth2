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
public class TimeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFilter adminSession;
    private Long storeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "admin@test.com", "password", "어드민", "ADMIN"
        );
        storeId = StoreFixture.insertDefaultStore(jdbcTemplate);
        adminSession = login("admin@test.com", "password");
    }

    @Test
    void 시간_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("reservationTimes.size()", is(1));

        RestAssured.given().log().all()
                .filter(adminSession)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약과_시간_연결() {
        SessionFilter userSession = createMemberAndLogin();

        Map<String, String> timeParams = new HashMap<>();
        timeParams.put("startAt", "10:00");
        RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "공포");
        themeParams.put("description", "무서운 테마");
        themeParams.put("thumbnailImageUrl", "https://example.com/horror.jpg");
        RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2099-12-31");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("storeId", storeId);

        RestAssured.given().log().all()
                .filter(userSession)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .filter(adminSession)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1));
    }

    private SessionFilter createMemberAndLogin() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "user@test.com", "password", "사용자", "USER"
        );
        return login("user@test.com", "password");
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
