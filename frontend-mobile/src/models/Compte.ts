export interface Compte {
    idCompte?: string;
    nom: string;
    email: string;
    mdp: string;
    profil: string;
    isBlocked: boolean;
    firebaseId: string;
    fcmTokens?: string [];
    lastSync?: Date;
    tentative?: number;
}
