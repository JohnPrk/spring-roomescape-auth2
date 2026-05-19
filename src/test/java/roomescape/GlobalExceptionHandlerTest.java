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

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class GlobalExceptionHandlerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFilter userSession;
    private SessionFilter adminSession;

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
        userSession = login("user@test.com", "password");
        adminSession = login("admin@test.com", "password");
    }

    @Test
    void 경로_변수_타입_불일치는_400() {
        RestAssured.given().log().all()
                .filter(adminSession)
                .when().delete("/admin/reservations/abc")
                .then().log().all()
                .statusCode(400)
                .body("type", is(ProblemType.BAD_REQUEST.uri().toString()));
    }

    @Test
    void 지원하지_않는_HTTP_메서드는_405() {
        RestAssured.given().log().all()
                .filter(adminSession)
                .when().patch("/admin/reservations/1")
                .then().log().all()
                .statusCode(405)
                .body("type", is(ProblemType.METHOD_NOT_SUPPORTED.uri().toString()));
    }

    @Test
    void 지원하지_않는_미디어_타입은_415() {
        RestAssured.given().log().all()
                .filter(userSession)
                .contentType(ContentType.TEXT)
                .body("not json")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(415)
                .body("type", is(ProblemType.MEDIA_TYPE_NOT_SUPPORTED.uri().toString()));
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
