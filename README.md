## 사이클1 - 기능목록

### 테마 도메인

* [x] 테마 도메인 추가
    * [x] 테마는 이름, 설명, 썸네일 이미지 url을 가진다
    * 모든 테마의 시작 시간과 소요 시간은 동일하다고 가정한다
* [x] 예약에 테마 정보가 포함 되도록 수정
* [x] 관리자의 테마 추가, 삭제

### 사용자 예약

* [x] 사용자의 날짜, 테마 선택 -> 예약 가능한 시간 표시
    * [x] 예약 가능한 시간이란 관리자가 등록한 시간 중 해당 날짜+테마에 아직 예약이 없는 시간이다
* [x] 사용자가 본인의 이름으로 예약 가능한 시간으로 예약
* [x] 같은 날짜, 시간이라도 테마가 다르면 각각 예약 가능

### 인기 테마 조회

* [x] 최근 1주일동안 예약이 많았던 테마 상위 10개 조회
    * [x] 최근 1주일을 이전 날 기준으로 조회한다

### 추가 요구사항

* [x] `data.sql`로 테스트용 데이터 넣어서 인기 테마 조회 결과 검증

### 화면

* [x] 사용자가 보는 화면 출력
    * [x] 브라우저에서 정상 동작 확인까지만 한다

## 사이클2 - 1단계 서비스 정책

* [x] 지난 날짜·시간 예약 거부
* [x] 같은 날짜·시간·테마 중복 예약 거부
* [x] 예약이 존재하는 시간 삭제 거부
* [x] 유효하지 않은 입력값 거부 (빈 이름, 잘못된 날짜/시간 형식)

## 사이클2 - 2단계 에러 응답 설계

* [x] 전역 예외 처리 도입 (`@RestControllerAdvice` / `@ExceptionHandler`)
* [x] 공통 에러 응답 본문 형식 (RFC 9457 Problem Details, `application/problem+json`)
* [x] 예외 계층 도입 (`NotFoundException` 404 / `ConflictException` 409 / `BusinessRuleViolationException` 422)
* [x] 처리되지 않은 예외가 500으로 새지 않도록 보강
* [x] 클라이언트 화면 에러 메시지 노출

## 사이클2 - 3단계 내 예약 조회/변경/취소

* [x] 본인 예약 목록 조회 (이름 기준)
* [x] 본인 예약 취소
* [x] 본인 예약 날짜·시간 변경
* [x] 변경·취소 엣지 케이스 처리 (지난 예약 거부, 시간 충돌 거부 등) — 2단계 ProblemDetail 규칙대로
* [x] README에 API 명세 및 에러 응답 명세 정리

## API 명세

성공 응답은 `application/json`, 에러 응답은 `application/problem+json`.

### 관리자 API

| 메서드 | 경로 | 요청 | 성공 응답 |
|---|---|---|---|
| GET | `/reservations` | `?page=0&size=20` (선택) | 200 `{ reservations: [...] }` |
| POST | `/reservations` | `{ name, date, timeId, themeId }` | 201 `{ id, name, date, time, theme }` |
| DELETE | `/reservations/{id}` | | 204 |
| GET | `/times` | | 200 `{ times: [...] }` |
| POST | `/times` | `{ startAt }` | 201 `{ id, startAt }` |
| DELETE | `/times/{id}` | | 204 |
| GET | `/themes` | | 200 `{ themes: [...] }` |
| POST | `/themes` | `{ name, description, thumbnailImageUrl }` | 201 `{ id, name, description, thumbnailImageUrl }` |
| DELETE | `/themes/{id}` | | 204 |
| GET | `/themes/popular` | `?now=YYYY-MM-DD&days=7&limit=10` (모두 선택) | 200 `{ themes: [...] }` |

### 사용자 API

| 메서드 | 경로 | 요청 | 성공 응답 |
|---|---|---|---|
| GET | `/times/availability` | `?date=YYYY-MM-DD&themeId={id}` | 200 `{ times: [{ id, startAt, reserved }] }` |
| GET | `/reservations/me` | `?name={이름}` | 200 `{ reservations: [...] }` |
| PUT | `/reservations/me/{id}` | `?name={이름}` + `{ date, timeId }` | 200 `{ id, name, date, time, theme }` |
| DELETE | `/reservations/me/{id}` | `?name={이름}` | 204 |

## 에러 응답

RFC 9457 Problem Details 형식. `Content-Type: application/problem+json`.

```json
{
  "type": "https://roomescape.example/problems/business-rule-violation",
  "title": "비즈니스 정책 위반",
  "status": 422,
  "detail": "지난 시각으로 예약을 변경할 수 없습니다.",
  "instance": "/reservations/me/3"
}
```

요청 본문 검증 실패(`400 validation-error`)는 `errors` 배열이 추가된다.

```json
{
  "type": "https://roomescape.example/problems/validation-error",
  "title": "요청 본문 검증 실패",
  "status": 400,
  "detail": "요청 본문의 일부 필드가 유효하지 않습니다.",
  "instance": "/reservations",
  "errors": [
    { "pointer": "/name", "reason": "이름은 비어 있을 수 없습니다." }
  ]
}
```

| 상태 | type slug | 발생 조건 |
|---|---|---|
| 400 | `validation-error` | 요청 본문이 `@Valid` 검증 실패 |
| 400 | `bad-request` | 필수 쿼리 파라미터 누락, 요청 본문 파싱 실패, 경로 변수 타입 불일치 등 일반 잘못된 요청 |
| 401 | `unauthorized` | 다른 사람 이름으로 본인 예약 변경·취소 시도 |
| 404 | `not-found` | 도메인 리소스 미존재 (예: 예약 id) |
| 404 | `no-resource` | 정적 리소스 미존재 (Spring MVC `NoResourceFoundException`) |
| 405 | `method-not-supported` | 지원하지 않는 HTTP 메서드 |
| 406 | `not-acceptable` | 응답 가능한 미디어 타입 없음 |
| 409 | `conflict` | 동일 날짜·시간·테마 중복 예약 |
| 415 | `media-type-not-supported` | 지원하지 않는 요청 미디어 타입 |
| 422 | `business-rule-violation` | 지난 시각 예약/변경, 예약이 존재하는 시간·테마 삭제 |
| 500 | `internal-error` | 처리되지 않은 예외 |

## 선택 미션 - 1단계 (인증/인가)

### 1. 로그인 상태 유지 방식

- HTTP Session(JSESSIONID) 기반. 토큰 발급/검증을 직접 구현하지 않고 서블릿 세션을 그대로 사용
- 로그인 성공 시 `AuthController`에서 `SessionStore.saveMemberId(session, member.getId())` 호출 → 세션에 memberId만 저장 (Member 객체 통째로 X)
- 매 요청마다 브라우저가 보낸 JSESSIONID 쿠키로 세션 복원 → 세션에서 memberId 추출 → DB 재조회로 Member 확보
- 로그아웃은 `DELETE /login/sessions`에서 `session.invalidate()`
- 세션 키 문자열(`"memberId"`)은 `SessionStore` 내부에 private static final로 캡슐화 → 외부 패키지는 `saveMemberId` / `findMemberId` 행위로만 접근

### 2. 인증이 필요한 API (`WebConfig.addInterceptors` 등록 패턴 기준)

**로그인만 필요 (`AuthInterceptor`):**
- `GET /members/me`
- `POST /reservations`
- `GET/PUT/DELETE /reservations/me/**`

**관리자 권한까지 필요 (`AdminInterceptor`):**
- `/admin/**` (예: `GET /admin/reservations`, `POST /admin/themes`, `POST /admin/times`)

**무인증으로 열려 있는 것:**
- `POST/DELETE /login/sessions` (로그인/로그아웃 자체)
- `GET /themes`, `GET /themes/popular`, `GET /reservations/<date>/...` (가용 시간 조회) 등 공개 조회

### 3. 공통 처리 위치

- **인증 검사**: `AuthInterceptor.preHandle` — 세션에 memberId 있는지만 검사
- **권한 검사**: `AdminInterceptor.preHandle` — 세션 → Member 조회 → `member.isAdmin()` 확인. 실패 시 401 / 403 분리
- **컨트롤러 파라미터 주입**: `@LoginMember Member` 한 줄로 인증된 사용자를 받음. 처리는 `LoginMemberArgumentResolver`에 일원화 → 컨트롤러에 세션 코드 누출 없음
- **브라우저 vs API 분기**: `BrowserRequest.isHtmlRequest`로 `Accept: text/html` 여부 판단 → HTML 요청이면 `/login?redirect=원본 URL`로 리다이렉트, JSON API 요청이면 `UnauthorizedException` throw
- **예외 응답 포맷팅**: `UnauthorizedException` / `ForbiddenException`은 `GlobalExceptionHandler`가 RFC 7807 ProblemDetail로 변환

즉 인증/인가는 인터셉터 두 개 + ArgumentResolver 하나에 모여 있고, 각 컨트롤러는 `@LoginMember Member`만 받으면 된다.

### 4. 테스트한 인증 실패 상황

**(1) 로그인 자체 실패** (`AuthApiTest`)
- 존재하지 않는 이메일 → 401 + `ProblemType.UNAUTHORIZED`
- 비밀번호 불일치 → 401 (동일 메시지로 사용자 존재 여부 노출 안 함)
- 이메일 필드 누락 (validation) → 400 + `ProblemType.VALIDATION_ERROR`

**(2) 미로그인 상태로 보호된 API 호출**
- `GET /members/me` 미로그인 → 401 (`MemberApiTest:34`)
- `POST /reservations` 미로그인 → 401 (`ReservationApiTest:298`)
- `GET /reservations/me` 미로그인 → 401 (`ReservationApiTest:371`)
- `GET /admin/reservations` 미로그인 → 401 (`ReservationApiTest:322`)

**(3) 권한 부족** (인증은 됐지만 admin 아님)
- 일반 사용자가 `GET /admin/reservations` → 403 + `ProblemType.FORBIDDEN` (`ReservationApiTest:312`)
- 일반 사용자가 `DELETE /admin/reservations/{id}` → 403 (`ReservationApiTest:330`)

**(4) 본인 자원 인가** (소유권 위반)
- 다른 사용자가 남의 예약 취소 `DELETE /reservations/me/{id}` → 401 (`ReservationApiTest:399`)
- 다른 사용자가 남의 예약 변경 `PUT /reservations/me/{id}` → 401 (`ReservationApiTest:473`)

**(5) 성공 경로** (대조 케이스)
- 로그인 → 동일 SessionFilter로 후속 요청 시 JSESSIONID 유지되며 200 / 본인 정보·예약 정상 응답 (`MemberApiTest:43`)
