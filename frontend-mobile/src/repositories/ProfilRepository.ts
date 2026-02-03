
import {
    collection,
    getDocs,
    getDoc,
    doc,
    setDoc,
    addDoc
} from "firebase/firestore";

import { Profil } from "../models/Profil";
import { db } from "@/firebase";

export class ProfilRepository {
    static COLLECTION = "profils";

    static async getAll(): Promise<Profil[]> {
        const snap = await getDocs(collection(db, ProfilRepository.COLLECTION));
        return snap.docs.map(d => ({
            idProfil: d.id,
            ...(d.data() as Omit<Profil, "idProfil">)
        }));
    }

    static async getByName(nom: string): Promise<Profil | null> {
        const profils = await this.getAll();
        const profil = profils.find(p => p.nom === nom);
        return profil || null;
    }

    static async getById(id: string): Promise<Profil | null> {
        const ref = doc(db, ProfilRepository.COLLECTION, id);
        const snap = await getDoc(ref);
        return snap.exists()
            ? { idProfil: snap.id, ...(snap.data() as Omit<Profil, "idProfil">) }
            : null;
    }

    static async create(profil: Profil): Promise<void> {
        const docRef = await addDoc(collection(db, ProfilRepository.COLLECTION), profil);
    }
}
