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
| 401 | `unauthorized` | 미인증 상태로 보호된 API 호출, 로그인 실패 |
| 403 | `forbidden` | 권한 부족(admin·manager 아님), 본인 소유가 아닌 예약 접근, 매니저의 다른 매장 예약 접근 |
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
- 다른 사용자가 남의 예약 취소 `DELETE /reservations/me/{id}` → 403 (`ReservationApiTest.다른_사용자로_본인_예약을_취소하면_403`)
- 다른 사용자가 남의 예약 변경 `PUT /reservations/me/{id}` → 403 (`ReservationApiTest.다른_사용자로_본인_예약을_변경하면_403`)
- 위 두 케이스는 3단계에서 401(인증 실패)에서 403(인가 실패)으로 정정 (로그인은 했으나 권한 없음이므로 Forbidden)

**(5) 성공 경로** (대조 케이스)
- 로그인 → 동일 SessionFilter로 후속 요청 시 JSESSIONID 유지되며 200 / 본인 정보·예약 정상 응답 (`MemberApiTest:43`)

## 선택 미션 - 2단계 (모바일 인증)

### 선택 도구

HMAC-SHA256 서명된 자체 액세스 토큰 + 세션/`Authorization` 헤더 통합 resolver.

- 토큰 포맷: `{memberId}:{expiresAtEpochMs}:{hex_signature}`
- 발급·검증: JDK 표준 `javax.crypto.Mac`만 사용 (외부 라이브러리 X)
- 라우팅: `MemberIdResolver`가 세션 우선 → 없으면 `Authorization: Bearer <token>` 헤더로 폴백 → `AuthInterceptor` · `AdminInterceptor` · `LoginMemberArgumentResolver` 셋이 같은 헬퍼를 공유
- 비밀키 · TTL은 `application.properties`의 `auth.token.secret` / `auth.token.ttl-minutes`로 외부화

### 다른 후보

- 표준 JWT 라이브러리 (jjwt, nimbus-jose-jwt 등)
- Spring Security + Spring Security OAuth2 Resource Server
- Access Token + Refresh Token 이중 토큰 구조
- 서버 측 토큰 무효화 저장소 (블랙리스트 / 세션 ID 매핑)

### 선택 이유

- 미션 스코프가 Spring Security · JWT 라이브러리 풀스택 · OAuth2 풀스택을 명시적으로 제외함. 라이브러리 통합이 아니라 *인증의 책임 경계*를 직접 그어보는 게 학습 목적
- JWT 라이브러리를 도입하면 토큰 포맷 협상 · 서명 · 검증 · 예외 매핑이 한 번에 블랙박스화됨. "왜 만료 시간이 필요한지", "왜 서명이 필요한지", "헤더 파싱과 세션 폴백을 어디서 합칠지" 같은 결정 지점을 못 만남
- 만료 정책은 *만료 시간만* 으로 가장 단순한 형태를 선택. 서버 측 무효화 저장소를 두지 않은 대신 TTL과 HMAC 서명으로 변조·재사용을 차단
- 세션 흐름(웹)을 그대로 두고 헤더 흐름(모바일)만 추가하기 위해 `MemberIdResolver` 하나를 추가하는 *최소 침습 리팩터*로 풀었음. 컨트롤러·인터셉터·ArgumentResolver는 웹/모바일을 구분하지 않음

### 불편하거나 아쉬운 점

- 토큰 시크릿을 `application.properties`와 테스트 픽스처(`TestAuthFixture`)에 평문으로 박아둘 수밖에 없어 키 회전 시 코드 동기화가 필요. 실제 운영이라면 환경 변수·KMS로 빼야 한다
- 서버 측 무효화 저장소가 없어 *로그아웃 즉시 토큰 무효화*는 불가능. 세션은 `session.invalidate()`로 즉시 무효화되지만, 토큰은 클라이언트가 버려도 만료 시각까지는 서명만 맞으면 유효 (만료시간만 정책의 trade-off)
- 토큰 포맷이 `{memberId}:{expiresAt}:{signature}`로 단순 문자열 분리에 의존. payload에 정보를 더 넣으려면 구분자 충돌·인코딩(Base64URL 등)을 직접 챙겨야 해서 확장성이 약함
- 토큰에 `iat`(issued at) · `iss`(issuer) 같은 메타데이터가 없어 *언제 발급된 토큰인지* 서버 로그·디버깅에서 추적 불가
- 만료 시각이 응답 body에 노출되지 않아 클라이언트가 토큰 만료를 *호출 후 401을 받고 나서야* 알 수 있음 (사전 재로그인 유도 불가)

## 선택 미션 - 3단계 (인가: 자기 매장 예약만 관리하기)

매장 매니저가 자기 매장의 예약만 조회·삭제하도록 인가를 추가했다. 다른 매장 예약에 접근하는 요청은 거부된다.

매니저 API:

| 메서드 | 경로 | 권한 | 성공 응답 |
|---|---|---|---|
| GET | `/manager/reservations` | MANAGER | 200 `{ reservations: [자기 매장만] }` |
| DELETE | `/manager/reservations/{id}` | MANAGER + 자기 매장 | 204 |

### 선택 도구

- 별도 보안 프레임워크(Spring Security) 없이 **HandlerInterceptor(역할 게이트) + 서비스 계층(자원 인가 판단)** 조합으로 인가를 직접 구현
- 매니저 모델링: `Role.MANAGER` enum 신설 + 별도 `Manager` 도메인 + `store_manager` 매핑 테이블(매니저-매장 1:1)
- 예약-매장 식별: `reservation.store_id` 직접 컬럼(NOT NULL + FK)
- 매니저 전용 라우트: `/manager/reservations` 신설(`ManagerReservationController`) + `ManagerInterceptor`(MANAGER만 통과)

### 다른 후보

- **Spring Security**: 역할 기반 접근 제어를 표준 제공하지만 학습 미션 범위 대비 설정·러닝커브가 과해, 인가 흐름을 직접 드러내는 쪽을 택함
- **기존 `ADMIN` 재활용**(ADMIN + store_id): 전체 운영자와 매장 운영자 의미가 흐려져 탈락
- **`Member.managedStoreId` 단일 컬럼**: USER/ADMIN row가 항상 NULL인 sparse 컬럼이 되어 탈락. 별도 `store_manager`로 정규화
- **`theme.store_id`로 예약 매장 도출**: 자연스럽지만 한 테마가 여러 매장에서 운영될 가능성을 닫음. 예약 단위로 매장을 명시하려 직접 컬럼 선택
- **도메인 객체에 인가 규칙**(`Reservation.verifyManagedBy(member)`): 도메인 응집도는 높지만 NotFound vs Forbidden 분기 같은 흐름 제어가 어색해 탈락
- **컨트롤러에서 인가 차단**: 인가 로직이 컨트롤러마다 흩어져 탈락

### 선택 이유

- USER/ADMIN/MANAGER 세 권한이 enum으로 명확히 분리되고, 매장 관계 정보는 `Manager` 도메인이 보유해 책임이 나뉨
- `store_manager` 정규화로 USER/ADMIN에 NULL 컬럼을 만들지 않고, 1:N 확장 시 PK를 `(member_id, store_id)` 복합키로 바꾸면 됨
- 예약이 `store_id`를 직접 가지므로 매장 검증이 `reservation.getStore().getId()` ↔ `Manager.managesStore(storeId)` 비교로 단순
- 인터셉터로 "로그인 + 역할"을, 서비스로 "이 자원이 내 매장인지"를 나눠 컨트롤러를 얇게 유지

### 인가 판단 위치

- **인증 여부 + 역할(MANAGER) 게이트** → `ManagerInterceptor`(`/manager/**`). 비로그인은 401, MANAGER가 아니면 403
- **자원 수준 인가**(이 예약이 매니저가 관리하는 매장의 것인가) → `ReservationService`의 private `verifyManagedBy`. 위반 시 `ForbiddenException`(403)
- `Manager.managesStore(storeId)` 도메인 헬퍼는 두되, 호출(조회 → 매장 비교 → 거부/허용)은 서비스가 함. 컨트롤러는 위임만
- 인증 실패(401)와 인가 실패(403)를 뭉개지 않도록, 본인 소유가 아닌 예약 접근(`findMyReservation`의 NOT_OWNER)도 `UnauthorizedException`에서 `ForbiddenException`으로 분리

### 유지하거나 변경하고 싶은 점

**유지하고 싶은 점**

- 인증 실패(401)와 인가 실패(403)의 명확한 분리
- 라우트 게이트(인터셉터)와 자원 인가(서비스)의 역할 분담, 컨트롤러는 얇게
- `store_manager` 정규화로 sparse 컬럼을 피한 점

**변경하고 싶은 점**

- 매니저-매장 1:1을 1:N으로 확장하면 `store_manager` PK를 복합키로 바꾸고, 단일 매장 필터(`findAllByStoreId`)를 여러 매장(`storeIds IN (...)`) 조회로 일반화해야 함
- `ReservationResponse`에 매장 정보가 없어 통합 테스트에서 자기 매장 필터링을 예약 날짜로 우회 검증했는데, 응답에 store를 노출하면 더 직접적으로 검증·표현 가능
- 매니저 라우트가 조회·삭제만 제공하므로, 운영 시나리오가 늘면 변경(승인/거절 등) 액션 확장 고려
