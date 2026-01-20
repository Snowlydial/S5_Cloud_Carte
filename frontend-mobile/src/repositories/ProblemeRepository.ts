import {
    collection,
    getDocs,
    getDoc,
    doc,
    addDoc,
    updateDoc,
    deleteDoc,
    query,
    where
} from "firebase/firestore";

import { Probleme } from "../models/Probleme";
import { db } from "@/firebase";

export class ProblemeRepository {
    static COLLECTION = "problemes";

    static async getAll(): Promise<Probleme[]> {
        const snap = await getDocs(collection(db, ProblemeRepository.COLLECTION));
        return snap.docs.map(d => ({
            idProbleme: d.id,
            ...(d.data() as Omit<Probleme, "idProbleme">)
        }));
    }

    static async getById(id: string): Promise<Probleme | null> {
        const ref = doc(db, ProblemeRepository.COLLECTION, id);
        const snap = await getDoc(ref);
        return snap.exists()
            ? { idProbleme: snap.id, ...(snap.data() as Omit<Probleme, "idProbleme">) }
            : null;
    }

    static async getByIdCompte(idCompte: string): Promise<Probleme[]> {
        const q = query(
            collection(db, ProblemeRepository.COLLECTION),
            where("idCompte", "==", idCompte)
        );
        const snap = await getDocs(q);
        return snap.docs.map(d => ({
            idProbleme: d.id,
            ...(d.data() as Omit<Probleme, "idProbleme">)
        }));
    }

    static async getByIdSignalement(idSignalement: string): Promise<Probleme[]> {
        const q = query(
            collection(db, ProblemeRepository.COLLECTION),
            where("idSignalement", "==", idSignalement)
        );
        const snap = await getDocs(q);
        return snap.docs.map(d => ({
            idProbleme: d.id,
            ...(d.data() as Omit<Probleme, "idProbleme">)
        }));
    }

    static async getByIdEntreprise(idEntreprise: string): Promise<Probleme[]> {
        const q = query(
            collection(db, ProblemeRepository.COLLECTION),
            where("idEntreprise", "==", idEntreprise)
        );
        const snap = await getDocs(q);
        return snap.docs.map(d => ({
            idProbleme: d.id,
            ...(d.data() as Omit<Probleme, "idProbleme">)
        }));
    }

    static async create(probleme: Omit<Probleme, "idProbleme">): Promise<string> {
        const docRef = await addDoc(collection(db, ProblemeRepository.COLLECTION), probleme);
        return docRef.id;
    }

    static async update(id: string, data: Partial<Omit<Probleme, "idProbleme">>): Promise<void> {
        const ref = doc(db, ProblemeRepository.COLLECTION, id);
        await updateDoc(ref, data);
    }

    static async delete(id: string): Promise<void> {
        const ref = doc(db, ProblemeRepository.COLLECTION, id);
        await deleteDoc(ref);
    }
}
