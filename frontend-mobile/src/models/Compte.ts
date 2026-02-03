export interface Compte {
    idCompte?: string;
    nom: string;
    email: string;
    mdp: string;
    idProfil: string;
    isBlocked: boolean;
    firebaseId: string;
    lastSync?: Date;
    tentative?: number;
}
