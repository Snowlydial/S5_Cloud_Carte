-- First, remove the foreign key constraint temporarily or delete users
TRUNCATE TABLE user_cloud CASCADE;
TRUNCATE TABLE profil CASCADE;

-- Then insert the correct profiles
INSERT INTO profil (nom) VALUES ('USER');
INSERT INTO profil (nom) VALUES ('ADMIN');
INSERT INTO profil (nom) VALUES ('MANAGER');
INSERT INTO profil (nom) VALUES ('SUPERVISOR');