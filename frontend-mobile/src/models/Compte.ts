export interface Compte {
    idCompte?: string;
    nom: string;
    email: string;
    mdp: string;
    profilId: string;
    tentative?: number;
}
