export interface Compte {
    idCompte?: string;
    nom: string;
    email: string;
    mdp: string;
    idProfil: string;
    isBlocked: boolean;
    firebaseId: string;
    fcmTokens?: string [];
    lastSync?: Date;
    tentative?: number;
}
