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
class MemberApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name) VALUES (?, ?, ?)",
                "user@test.com", "password", "사용자"
        );
    }

    @Test
    void 로그인_없이_내정보_조회는_401() {
        RestAssured.given().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void 로그인_후_내정보_조회는_200() {
        SessionFilter sessionFilter = new SessionFilter();

        Map<String, String> login = Map.of(
                "email", "user@test.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(login)
                .when().post("/login/sessions")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("email", is("user@test.com"))
                .body("name", is("사용자"));
    }
}
