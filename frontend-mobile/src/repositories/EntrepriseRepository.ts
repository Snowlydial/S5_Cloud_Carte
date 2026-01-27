import {
    collection,
    getDocs,
    getDoc,
    doc,
    setDoc,
    addDoc,
    updateDoc,
    deleteDoc
} from "firebase/firestore";

import { Entreprise } from "../models/Entreprise";
import { db } from "@/firebase";

export class EntrepriseRepository {
    static COLLECTION = "entreprises";

    static async getAll(): Promise<Entreprise[]> {
        const snap = await getDocs(collection(db, EntrepriseRepository.COLLECTION));
        return snap.docs.map(d => ({
            idEntreprise: d.id,
            ...(d.data() as Omit<Entreprise, "idEntreprise">)
        }));
    }

    static async getById(id: string): Promise<Entreprise | null> {
        const ref = doc(db, EntrepriseRepository.COLLECTION, id);
        const snap = await getDoc(ref);
        return snap.exists()
            ? { idEntreprise: snap.id, ...(snap.data() as Omit<Entreprise, "idEntreprise">) }
            : null;
    }

    static async getByName(nom: string): Promise<Entreprise | null> {
        const entreprises = await this.getAll();
        const entreprise = entreprises.find(e => e.nom === nom);
        return entreprise || null;
    }

    static async create(entreprise: Omit<Entreprise, "idEntreprise">): Promise<string> {
        const docRef = await addDoc(collection(db, EntrepriseRepository.COLLECTION), entreprise);
        return docRef.id;
    }

    static async update(id: string, data: Partial<Omit<Entreprise, "idEntreprise">>): Promise<void> {
        const ref = doc(db, EntrepriseRepository.COLLECTION, id);
        await updateDoc(ref, data);
    }

    static async delete(id: string): Promise<void> {
        const ref = doc(db, EntrepriseRepository.COLLECTION, id);
        await deleteDoc(ref);
    }
}
