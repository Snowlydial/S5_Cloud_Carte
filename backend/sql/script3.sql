INSERT INTO type_signalement (nom) VALUES
('Nid de poule'),
('Route degradee'),
('Inondation'),
('eboulement'),
('Caniveau bouche'),
('Signalisation absente'),
('Chaussee fissuree');

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


-- Mettre à jour les signalements avec le type correct
UPDATE signalement
SET id_type = ts.id_type
FROM type_signalement ts
WHERE 
    -- Nid de poule
    (signalement.description ILIKE '%nid de poule%' AND ts.nom = 'Nid de poule')
    OR
    -- Route dégradée
    (signalement.description ILIKE '%route fortement degradee%' AND ts.nom = 'Route degradee')
    OR
    -- Inondation
    (signalement.description ILIKE '%inondee%' AND ts.nom = 'Inondation')
    OR
    -- Eboulement
    (signalement.description ILIKE '%eboulement%' AND ts.nom = 'eboulement')
    OR
    -- Caniveau bouche
    (signalement.description ILIKE '%caniveau%' AND ts.nom = 'Caniveau bouche')
    OR
    -- Signalisation absente
    (signalement.description ILIKE '%signalisation absente%' AND ts.nom = 'Signalisation absente')
    OR
    -- Chaussée fissurée
    (signalement.description ILIKE '%chaussee fissuree%' AND ts.nom = 'Chaussee fissuree');
