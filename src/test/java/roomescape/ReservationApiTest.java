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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFilter minwook;
    private SessionFilter tinue;
    private Long minwookId;
    private Long tinueId;

    @BeforeEach
    void setUp() {
        minwookId = insertMember("minwook@test.com", "password", "민욱");
        tinueId = insertMember("tinue@test.com", "password", "티뉴");
        minwook = login("minwook@test.com", "password");
        tinue = login("tinue@test.com", "password");
    }

    @Test
    void 예약_조회_빈목록() {
        RestAssured.given().log().all()
                .filter(minwook)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("totalCount", is(0))
                .body("page", is(0))
                .body("size", is(20));
    }

    @Test
    void 예약_추가() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = reservationBody("2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("민욱"))
                .body("date", is("2026-08-05"));
    }

    @Test
    void 예약_추가_후_조회() {
        Integer timeId = createTime("14:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");

        RestAssured.given()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-09-01", timeId, themeId))
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .filter(minwook)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("totalCount", is(1))
                .body("reservations[0].name", is("민욱"));
    }

    @Test
    void 예약_추가_및_삭제() {
        Integer timeId = createTime("18:00");
        Integer themeId = createTheme("SF", "우주에서 탈출", "https://example.com/sf.jpg");

        Integer reservationId = RestAssured.given()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-10-10", timeId, themeId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .filter(minwook)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .filter(minwook)
                .when().get("/reservations")
                .then()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("totalCount", is(0));
    }

    @Test
    void 페이지_크기보다_많은_예약이_있으면_size만큼만_반환된다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        for (int i = 1; i <= 5; i++) {
            RestAssured.given()
                    .filter(minwook)
                    .contentType(ContentType.JSON)
                    .body(reservationBody("2026-08-0" + i, timeId, themeId))
                    .when().post("/reservations");
        }

        RestAssured.given().log().all()
                .filter(minwook)
                .when().get("/reservations?page=0&size=3")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(3))
                .body("totalCount", is(5))
                .body("page", is(0))
                .body("size", is(3));
    }

    @Test
    void 두번째_페이지_조회시_나머지_예약이_반환된다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        for (int i = 1; i <= 5; i++) {
            RestAssured.given()
                    .filter(minwook)
                    .contentType(ContentType.JSON)
                    .body(reservationBody("2026-08-0" + i, timeId, themeId))
                    .when().post("/reservations");
        }

        RestAssured.given().log().all()
                .filter(minwook)
                .when().get("/reservations?page=1&size=3")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("totalCount", is(5))
                .body("page", is(1))
                .body("size", is(3));
    }

    @Test
    void 존재하지_않는_날짜로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-02-31", timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 윤년이_아닌_해의_2월_29일로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-02-29", timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void timeId가_누락된_요청으로_예약하면_400() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-08-05");
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 범위를_벗어난_월로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-13-01", timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 같은_날짜_시간_테마로_중복_예약하면_409() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = reservationBody("2026-08-05", timeId, themeId);

        RestAssured.given()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .filter(tinue)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("type", is(ProblemType.CONFLICT.uri().toString()));
    }

    @Test
    void 지난_날짜로_예약하면_422() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2020-01-01", timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 존재하지_않는_시간_ID로_예약하면_404() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-08-05", 9999, themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_테마_ID로_예약하면_404() {
        Integer timeId = createTime("11:00");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-08-05", timeId, 9999))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 로그인_없이_예약_생성하면_401() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationBody("2026-08-05", timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void 본인_예약_조회는_로그인한_사용자의_예약만_반환한다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        createReservation(minwook, "2026-08-05", timeId, themeId);
        Integer time2 = createTime("15:00");
        createReservation(tinue, "2026-08-05", time2, themeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("민욱"));
    }

    @Test
    void 본인_예약이_없으면_빈_목록이_반환된다() {
        RestAssured.given().log().all()
                .filter(minwook)
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 로그인_없이_내_예약_조회하면_401() {
        RestAssured.given().log().all()
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 본인_예약_취소는_204를_반환한다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .when().delete("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .filter(minwook)
                .when().get("/reservations/me")
                .then()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 다른_사용자로_본인_예약을_취소하면_401() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .filter(tinue)
                .when().delete("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 존재하지_않는_예약을_취소하면_404() {
        RestAssured.given().log().all()
                .filter(minwook)
                .when().delete("/reservations/me/9999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 지난_예약을_취소하면_422() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Long reservationId = insertPastReservation(minwookId, "2020-01-01", timeId, themeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .when().delete("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 본인_예약_변경은_200을_반환한다() {
        Integer timeId = createTime("13:00");
        Integer newTimeId = createTime("15:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", newTimeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 같은_슬롯으로_변경해도_충돌로_보지_않는다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-08-05");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 다른_사용자로_본인_예약을_변경하면_401() {
        Integer timeId = createTime("13:00");
        Integer newTimeId = createTime("15:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", newTimeId);

        RestAssured.given().log().all()
                .filter(tinue)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(401)
                .body("type", is(ProblemType.UNAUTHORIZED.uri().toString()));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_404() {
        Integer timeId = createTime("13:00");

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/9999")
                .then().log().all()
                .statusCode(404)
                .body("type", is(ProblemType.NOT_FOUND.uri().toString()));
    }

    @Test
    void 같은_날짜_테마에_다른_예약이_있는_시간으로_변경하면_409() {
        Integer timeId = createTime("13:00");
        Integer otherTimeId = createTime("15:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);
        createReservation(tinue, "2026-08-05", otherTimeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-08-05");
        body.put("timeId", otherTimeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 지난_시각으로_변경하면_422() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2020-01-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(422)
                .body("type", is(ProblemType.BUSINESS_RULE_VIOLATION.uri().toString()));
    }

    @Test
    void 이미_지난_예약을_변경하면_422() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Long reservationId = insertPastReservation(minwookId, "2020-01-01", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", timeId);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 변경_요청에_timeId가_누락되면_400() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 변경_요청의_새_시간_ID가_존재하지_않으면_404() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer reservationId = createReservation(minwook, "2026-08-05", timeId, themeId);

        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-09-01");
        body.put("timeId", 9999);

        RestAssured.given().log().all()
                .filter(minwook)
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/me/" + reservationId)
                .then().log().all()
                .statusCode(404);
    }

    private Map<String, Object> reservationBody(String date, Integer timeId, Integer themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        return params;
    }

    private Integer createReservation(SessionFilter session, String date, Integer timeId, Integer themeId) {
        return RestAssured.given()
                .filter(session)
                .contentType(ContentType.JSON)
                .body(reservationBody(date, timeId, themeId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Long insertPastReservation(Long memberId, String date, Integer timeId, Integer themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)",
                LocalDate.parse(date), timeId, themeId, memberId
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE member_id = ? AND date = ?",
                Long.class, memberId, LocalDate.parse(date)
        );
    }

    private Long insertMember(String email, String password, String name) {
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name) VALUES (?, ?, ?)",
                email, password, name
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = ?",
                Long.class, email
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
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
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
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }
}
