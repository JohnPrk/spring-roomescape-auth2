package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ProblemType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthApiTest {

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
    void 로그인_성공_시_JSESSIONID_쿠키가_발급된다() {
        Map<String, String> body = Map.of(
                "email", "user@test.com",
                "password", "password"
        );

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().log().all()
                .statusCode(200)
                .extract();

        assertThat(response.cookie("JSESSIONID")).isNotBlank();
    }

    @Test
    void 로그인_성공_시_응답_body에_accessToken이_포함된다() {
        Map<String, String> body = Map.of(
                "email", "user@test.com",
                "password", "password"
        );

        String accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        assertThat(accessToken).isNotBlank();
    }

    @Test
    void 존재하지_않는_이메일이면_401() {
        Map<String, String> body = Map.of(
                "email", "nobody@test.com",
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void 비밀번호가_틀리면_401() {
        Map<String, String> body = Map.of(
                "email", "user@test.com",
                "password", "wrong"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void 이메일_누락이면_400_validation_error() {
        Map<String, String> body = Map.of(
                "password", "password"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().log().all()
                .statusCode(400)
                .body("type", is(ProblemType.VALIDATION_ERROR.uri().toString()));
    }
}
