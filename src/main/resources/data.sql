-- store
INSERT INTO store (name)
VALUES ('강남점');
INSERT INTO store (name)
VALUES ('홍대점');
INSERT INTO store (name)
VALUES ('잠실점');

-- member
INSERT INTO member (email, password, name, role)
VALUES ('admin@test.com', 'password', '어드민', 'ADMIN');
INSERT INTO member (email, password, name, role)
VALUES ('user@test.com', 'password', '사용자', 'USER');
INSERT INTO member (email, password, name, role)
VALUES ('manager.gangnam@test.com', 'password', '강남점 매니저', 'MANAGER');
INSERT INTO member (email, password, name, role)
VALUES ('manager.hongdae@test.com', 'password', '홍대점 매니저', 'MANAGER');

-- store_manager (매니저-매장 1:1 할당)
INSERT INTO store_manager (member_id, store_id)
VALUES (3, 1);
INSERT INTO store_manager (member_id, store_id)
VALUES (4, 2);

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00');
INSERT INTO reservation_time (start_at)
VALUES ('15:00');
INSERT INTO reservation_time (start_at)
VALUES ('16:00');
INSERT INTO reservation_time (start_at)
VALUES ('17:00');
INSERT INTO reservation_time (start_at)
VALUES ('18:00');
INSERT INTO reservation_time (start_at)
VALUES ('19:00');

-- theme
INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('미스터리 저택', '빅토리아 시대 영국의 의문스러운 저택을 탐험하세요', 'https://loremflickr.com/800/600/mansion,dark');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('우주 정거장', '고장난 우주 정거장에서 살아남으세요', 'https://loremflickr.com/800/600/spacestation,space');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('좀비 아포칼립스', '좀비가 점령한 도시에서 탈출하세요', 'https://loremflickr.com/800/600/zombie,apocalypse');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('고대 이집트', '파라오의 무덤에 숨겨진 비밀을 풀어내세요', 'https://loremflickr.com/800/600/egypt,pyramid');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('해적선의 보물', '카리브해 해적선에서 보물을 찾아 탈출하세요', 'https://loremflickr.com/800/600/pirate');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('폐쇄 병동', '버려진 병동의 어두운 비밀을 파헤치세요', 'https://loremflickr.com/800/600/abandoned,asylum');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('시간 여행자의 실험실', '시간 여행 실험에 갇힌 당신을 구하세요', 'https://loremflickr.com/800/600/laboratory,steampunk');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('마법사의 탑', '사라진 마법사의 탑에서 주문을 풀어내세요', 'https://loremflickr.com/800/600/wizard,tower');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('침몰하는 잠수함', '가라앉는 잠수함에서 탈출하세요', 'https://loremflickr.com/800/600/submarine,ocean');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('은행 금고', '삼엄한 경비를 뚫고 금고에서 탈출하세요', 'https://loremflickr.com/800/600/bank,vault');

-- reservation (모두 member_id = 2 사용자 소유)
-- 인기 테마 윈도우 (오늘=2026-05-06 기준, 어제부터 7일 = 2026-04-29 ~ 2026-05-05)
-- 윈도우 내 카운트 목표:
--   theme 1: 5건, theme 5: 4건, theme 8: 4건, theme 3: 3건, theme 4: 3건,
--   theme 2: 2건, theme 7: 2건, theme 6: 1건, theme 9: 1건, theme 10: 1건

-- 윈도우 내 (기존)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-05', 3, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-05', 5, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-04', 7, 3, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-04', 4, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-03', 6, 5, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-03', 8, 4, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-02', 2, 7, 2);

-- 윈도우 내 (theme 1: +3건 → 총 5건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-04-29', 3, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-04-30', 2, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-01', 1, 1, 2);

-- 윈도우 내 (theme 5: +3건 → 총 4건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-05', 1, 5, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-04', 2, 5, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-02', 3, 5, 2);

-- 윈도우 내 (theme 8: +4건 → 총 4건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-04-30', 3, 8, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-01', 2, 8, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-03', 1, 8, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-05', 9, 8, 2);

-- 윈도우 내 (theme 3: +2건 → 총 3건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-02', 8, 3, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-05', 7, 3, 2);

-- 윈도우 내 (theme 4: +2건 → 총 3건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-01', 4, 4, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-04', 8, 4, 2);

-- 윈도우 내 (theme 2: +1건 → 총 2건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-04', 6, 2, 2);

-- 윈도우 내 (theme 7: +1건 → 총 2건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-04-30', 5, 7, 2);

-- 윈도우 내 (theme 6: +1건 → 총 1건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-03', 4, 6, 2);

-- 윈도우 내 (theme 9: +1건 → 총 1건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-01', 5, 9, 2);

-- 윈도우 내 (theme 10: +1건 → 총 1건)
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-04-30', 6, 10, 2);

-- 윈도우 직전 (2026-04-28) — 카운트에 포함되면 안 됨
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-04-28', 7, 1, 2);

-- 오늘 (2026-05-06) — end가 어제(05-05)이므로 카운트에서 제외되어야 함
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-06', 8, 5, 2);

-- 미래 — 윈도우 밖
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-14', 9, 8, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-14', 5, 6, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2026-05-15', 6, 10, 2);
