CREATE TABLE profil(
   id_profil SERIAL,
   nom VARCHAR(50) ,
   PRIMARY KEY(id_profil)
);

CREATE TABLE compte(
   id_compte SERIAL,
   nom VARCHAR(50) ,
   mdp VARCHAR(50) ,
   email VARCHAR(50) ,
   id_profil INTEGER NOT NULL,
   PRIMARY KEY(id_compte),
   FOREIGN KEY(id_profil) REFERENCES profil(id_profil)
);

CREATE TABLE signalement(
   id_signalement SERIAL,
   date_signalement TIMESTAMP NOT NULL,
   longitude NUMERIC(11,8)  ,
   latitude NUMERIC(11,8)   NOT NULL,
   id_compte INTEGER NOT NULL,
   PRIMARY KEY(id_signalement),
   FOREIGN KEY(id_compte) REFERENCES compte(id_compte)
);

CREATE TABLE entreprise(
   id_entreprise SERIAL,
   nom VARCHAR(50) ,
   PRIMARY KEY(id_entreprise)
);

CREATE TABLE status(
   id_status SERIAL,
   nom VARCHAR(50) ,
   PRIMARY KEY(id_status)
);

CREATE TABLE probleme(
   id_probleme SERIAL,
   date_probleme TIMESTAMP NOT NULL,
   surface_m2 NUMERIC(15,2)   NOT NULL,
   budget NUMERIC(15,2)   NOT NULL,
   id_entreprise INTEGER,
   id_compte INTEGER NOT NULL,
   id_signalement INTEGER NOT NULL,
   PRIMARY KEY(id_probleme),
   FOREIGN KEY(id_entreprise) REFERENCES entreprise(id_entreprise),
   FOREIGN KEY(id_compte) REFERENCES compte(id_compte),
   FOREIGN KEY(id_signalement) REFERENCES signalement(id_signalement)
);

CREATE TABLE type_signalement(
   id_type_signalement SERIAL,
   nom VARCHAR(50)  NOT NULL,
   PRIMARY KEY(id_type_signalement)
);

CREATE TABLE probleme_status(
   id_probleme_status SERIAL,
   etat VARCHAR(50) ,
   date_status TIMESTAMP,
   id_probleme INTEGER NOT NULL,
   id_status INTEGER NOT NULL,
   PRIMARY KEY(id_probleme_status),
   FOREIGN KEY(id_probleme) REFERENCES probleme(id_probleme),
   FOREIGN KEY(id_status) REFERENCES status(id_status)
);

CREATE TABLE Asso_8_1(
   id_signalement INTEGER,
   id_type_signalement INTEGER,
   PRIMARY KEY(id_signalement, id_type_signalement),
   FOREIGN KEY(id_signalement) REFERENCES signalement(id_signalement),
   FOREIGN KEY(id_type_signalement) REFERENCES type_signalement(id_type_signalement)
);
