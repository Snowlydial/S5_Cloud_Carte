export interface Compte {
    id?: string;
    nom: string;
    email: string;
    mdp: string;
    profilId: string;
    tentative?: number;
}
