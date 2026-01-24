INSERT INTO type_signalement (nom) VALUES
('Nid de poule'),
('Route degradee'),
('Inondation'),
('eboulement'),
('Caniveau bouche'),
('Signalisation absente'),
('Chaussee fissuree');

UPDATE signalement
SET description = 'Presence d un grand nid de poule rendant la circulation difficile, risque eleve pour les vehicules.'
WHERE id_signalement = 232;

UPDATE signalement
SET description = 'Route fortement degradee avec plusieurs fissures, circulation ralentie.'
WHERE id_signalement = 233;

UPDATE signalement
SET description = 'Petit nid de poule en formation, intervention rapide recommandee.'
WHERE id_signalement = 234;

UPDATE signalement
SET description = 'Zone inondee après de fortes pluies, passage presque impraticable.'
WHERE id_signalement = 235;

UPDATE signalement
SET description = 'Chaussee endommagee avec affaissement partiel, danger pour les deux-roues.'
WHERE id_signalement = 236;

WITH derniers AS (
    SELECT id_signalement
    FROM signalement
    ORDER BY id_signalement DESC
    LIMIT 5
)
UPDATE signalement s
SET description = v.description
FROM (
    SELECT id_signalement,
           ROW_NUMBER() OVER (ORDER BY id_signalement DESC) AS rn
    FROM derniers
) d
JOIN (
    VALUES
        (1, 'Presence d un grand nid de poule rendant la circulation difficile, risque eleve pour les vehicules.'),
        (2, 'Route fortement degradee avec plusieurs fissures, circulation ralentie.'),
        (3, 'Petit nid de poule en formation, intervention rapide recommandee.'),
        (4, 'Zone inondee après de fortes pluies, passage presque impraticable.'),
        (5, 'Chaussee endommagee avec affaissement partiel, danger pour les deux-roues.')
) v(rn, description)
ON d.rn = v.rn
WHERE s.id_signalement = d.id_signalement;
