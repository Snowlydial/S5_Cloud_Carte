INSERT INTO signalement 
(date_signalement, last_sync, latitude, longitude, surface_m2, id_compte)
VALUES
('2026-01-19 10:15:00', '2026-01-19 12:00:00', -18.8792, 47.5079, 120, 5),
('2026-01-18 09:30:00', '2026-01-18 11:00:00', -18.9200, 47.5000, 200, 5),
('2026-01-20 14:45:00', '2026-01-20 15:30:00', -18.9150, 47.5100, 50, 5),
('2026-01-17 16:00:00', '2026-01-17 18:00:00', -18.9100, 47.5050, 80, 5),
('2026-01-19 08:00:00', '2026-01-19 09:00:00', -18.9300, 47.5200, 150, 5);

-- Entreprises de test
INSERT INTO entreprise (id_entreprise, nom) VALUES
(1, 'Alpha Construction'),
(2, 'BTP Solutions'),
(3, 'Eco Renovation');

-- Statuts de test
INSERT INTO status (id_status, nom) VALUES
(1, 'EN_ATTENTE'),
(2, 'EN_COURS'),
(3, 'TERMINE');
