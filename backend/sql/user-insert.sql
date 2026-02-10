INSERT INTO profil (id_profil, nom, firebase_id, last_sync)
VALUES
    (7, 'MANAGER', 'GelaSItj2US08L7zQnrq', '2026-02-10 08:52:54.49141'),
    (8, 'USER', 'Ml4wfT6KUrCQirVta4YC', '2026-02-10 08:52:54.491794'),
    (9, 'SUPERVISOR', 'TaauuLycjyd8SEadKHJ', '2026-02-10 08:52:54.491897'),
    (10, 'ADMIN', 'buYgvoz1xZBctpKF1q6y', '2026-02-10 08:52:54.491977');

INSERT INTO user_cloud (
    id, created_at, email, firebase_uid, is_blocked, last_sync,
    login_attempts, password_plain, role, id_profil
)
VALUES
    (27, '2026-02-10 08:39:15.9106', 'toto@gmail.com', '0LzblfQyRPfhSRK1MVVvDapG4iU2', FALSE, '2026-02-03 05:05:39.088904', 0, '111111', 'USER', 8),
    (28, '2026-02-10 08:39:15.921286', 'mims@gmail.com', '1iV7VAUAYqj7q6py1c5r', FALSE, '2026-02-03 04:51:10.797245', 0, '111111', 'USER', 8),
    (29, '2026-02-10 08:39:15.930166', 'mioty@gmail.com', '3B1suTCwsbUlCgyoR1JibNtp25p2', FALSE, '2026-02-03 04:51:10.796197', 2, '111111', 'USER', 8),
    (30, '2026-02-10 08:39:15.937705', 'ryan@gmail.com', 'FFNgyMqRrYU2dDfu9apSCmtvfcB2', FALSE, '2026-02-03 08:36:26.966513', 0, '111111', 'MANAGER', 7),
    (31, '2026-02-10 08:39:15.945695', 'nyavo@gmail.com', 'kE9zsmGOHn71zqY7pseb', FALSE, '2026-02-10 05:11:45.758262', 0, '111111', 'USER', 8),
    (32, '2026-02-10 08:39:15.954464', 'mimi@gmail.com', 'zuHQGNm0ejQlE9J0EVdMYMlH7Aj1', FALSE, '2026-02-03 04:51:10.794587', 0, '111111', 'MANAGER', 7);
