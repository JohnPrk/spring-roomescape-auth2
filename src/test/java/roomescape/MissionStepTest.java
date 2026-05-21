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
public class MissionStepTest {

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
    void 예약_조회() {
        RestAssured.given().log().all()
                .filter(adminSession)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 예약_추가_및_삭제() {
        Map<String, String> timeParams = new HashMap<>();
        timeParams.put("startAt", "15:40");

        Integer timeId = RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");

        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "공포");
        themeParams.put("description", "무서운 테마");
        themeParams.put("thumbnailImageUrl", "https://example.com/horror.jpg");

        Integer themeId = RestAssured.given().log().all()
                .filter(adminSession)
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");

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
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .filter(adminSession)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1));

        RestAssured.given().log().all()
                .filter(adminSession)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .filter(adminSession)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
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
