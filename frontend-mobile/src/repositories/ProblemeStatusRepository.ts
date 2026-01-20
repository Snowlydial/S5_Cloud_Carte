import {
    collection,
    getDocs,
    getDoc,
    doc,
    addDoc,
    updateDoc,
    deleteDoc,
    query,
    where,
    orderBy,
    limit,
    Timestamp
} from "firebase/firestore";

import { ProblemeStatus } from "../models/ProblemeStatus";
import { db } from "@/firebase";

export class ProblemeStatusRepository {
    static COLLECTION = "probleme_status";

    static async getAll(): Promise<ProblemeStatus[]> {
        const snap = await getDocs(collection(db, ProblemeStatusRepository.COLLECTION));
        return snap.docs.map(d => ({
            idProblemeStatus: d.id,
            ...(d.data() as Omit<ProblemeStatus, "idProblemeStatus">)
        }));
    }

    static async getById(id: string): Promise<ProblemeStatus | null> {
        const ref = doc(db, ProblemeStatusRepository.COLLECTION, id);
        const snap = await getDoc(ref);
        return snap.exists()
            ? { idProblemeStatus: snap.id, ...(snap.data() as Omit<ProblemeStatus, "idProblemeStatus">) }
            : null;
    }

    static async getByIdProbleme(idProbleme: string): Promise<ProblemeStatus[]> {
        const q = query(
            collection(db, ProblemeStatusRepository.COLLECTION),
            where("idProbleme", "==", idProbleme)
        );
        const snap = await getDocs(q);
        return snap.docs.map(d => ({
            id: d.id,
            ...(d.data() as Omit<ProblemeStatus, "id">)
        }));
    }

    static async getByIdStatus(idStatus: string): Promise<ProblemeStatus[]> {
        const q = query(
            collection(db, ProblemeStatusRepository.COLLECTION),
            where("idStatus", "==", idStatus)
        );
        const snap = await getDocs(q);
        return snap.docs.map(d => ({
            idProblemeStatus: d.id,
            ...(d.data() as Omit<ProblemeStatus, "idProblemeStatus">)
        }));
    }

   

    static async create(problemeStatus: Omit<ProblemeStatus, "idProblemeStatus">): Promise<string> {
        const docRef = await addDoc(collection(db, ProblemeStatusRepository.COLLECTION), problemeStatus);
        return docRef.id;
    }

    static async update(id: string, data: Partial<Omit<ProblemeStatus, "idProblemeStatus">>): Promise<void> {
        const ref = doc(db, ProblemeStatusRepository.COLLECTION, id);
        await updateDoc(ref, data);
    }

    static async delete(id: string): Promise<void> {
        const ref = doc(db, ProblemeStatusRepository.COLLECTION, id);
        await deleteDoc(ref);
    }
}
