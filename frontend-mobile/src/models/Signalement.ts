export interface Signalement {
    idSignalement?: string;
    dateSignalement: Date;
    longitude: number;
    latitude: number;
    idCompte: string  |  number;
    idTypeSignalement: number | string;
}