INSERT INTO users (id, name, email, password_hash, role)
VALUES
(1, '山田花子', 'hanakoyama@gmail.com', '$2a$10$boCZZD6yFIm1rQW8y7xP0.hknPiNx1Df03FR/JCDZYrCGXIEFkS2S', 'ADMIN'),
(2, '鈴木太郎', 'tarosuzuki@gmail.com', '$2a$10$5QjLHffMYShnPWHtQHbmgeXixaem/e7GQmjHdIT.PEXey7CW9q.la', 'MEMBER');
 
-- BIGSERIAL(id)へ明示的に値を挿入すると、内部シーケンスがそれを認識しないため、
-- 後続のアプリからのINSERT（サインアップ等）でid重複エラーになる可能性がある。
-- 挿入後に現在の最大値に合わせてシーケンスを補正しておく。
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));