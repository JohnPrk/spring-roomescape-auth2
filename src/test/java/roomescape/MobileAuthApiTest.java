package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.TokenProvider;
import roomescape.exception.ProblemType;
import roomescape.support.TestAuthFixture;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MobileAuthApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long minwookId;

    @BeforeEach
    void setUp() {
        minwookId = insertMember("minwook@test.com", "password", "민욱", "USER");
        insertMember("admin@test.com", "password", "어드민", "ADMIN");
    }

    @Test
    void Authorization_헤더의_토큰으로_members_me에_접근하면_200() {
        String token = login("minwook@test.com", "password");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + token)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("name", is("민욱"));
    }

    @Test
    void Authorization_헤더의_토큰으로_예약_생성하면_201() {
        String userToken = login("minwook@test.com", "password");
        String adminToken = login("admin@test.com", "password");
        Integer timeId = createTime(adminToken, "11:00");
        Integer themeId = createTheme(adminToken, "공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-08-05");
        body.put("timeId", timeId);
        body.put("themeId", themeId);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("민욱"));
    }

    @Test
    void Authorization_헤더_없이_보호_엔드포인트_호출하면_401() {
        RestAssured.given().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void Bearer_prefix가_없으면_401() {
        String token = login("minwook@test.com", "password");

        RestAssured.given().log().all()
                .header("Authorization", token)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 서명이_틀린_토큰으로_요청하면_401() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer 1:99999999999999:badsignature")
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 만료된_토큰으로_요청하면_401() {
        TokenProvider expiredIssuer = new TokenProvider(TestAuthFixture.tokenSecret(), 0);
        String expiredToken = expiredIssuer.issue(minwookId);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + expiredToken)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    private String login(String email, String password) {
        Map<String, String> body = Map.of("email", email, "password", password);
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login/sessions")
                .then().statusCode(200)
                .extract().jsonPath().getString("accessToken");
    }

    private Integer createTime(String adminToken, String startAt) {
        Map<String, String> params = Map.of("startAt", startAt);
        return RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTheme(String adminToken, String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = Map.of(
                "name", name,
                "description", description,
                "thumbnailImageUrl", thumbnailImageUrl
        );
        return RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
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
}
