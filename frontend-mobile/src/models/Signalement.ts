export interface Signalement {
    idSignalement?: number;
    dateSignalement: Date;
    longitude: number;
    latitude: number;
    idCompte: string  |  number;
    idTypeSignalement: number | string;
}