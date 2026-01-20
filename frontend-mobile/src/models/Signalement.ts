export interface Signalement {
    idSignalement?: string;
    dateSignalement: Date;
    longitude: number;
    latitude: number;
    idCompte: string  |  null;
    idTypeSignalement: number | string;
    idimage: string | null;
}