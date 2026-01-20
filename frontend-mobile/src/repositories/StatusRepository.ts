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

import { Status } from "../models/Status";
import { db } from "@/firebase";

export class StatusRepository {
    static COLLECTION = "status";

    static async getAll(): Promise<Status[]> {
        const snap = await getDocs(collection(db, StatusRepository.COLLECTION));
        return snap.docs.map(d => ({
            id: d.id,
            ...(d.data() as Omit<Status, "id">)
        }));
    }

    static async getById(id: string): Promise<Status | null> {
        const ref = doc(db, StatusRepository.COLLECTION, id);
        const snap = await getDoc(ref);
        return snap.exists()
            ? { id: snap.id, ...(snap.data() as Omit<Status, "id">) }
            : null;
    }

    static async getByName(nom: string): Promise<Status | null> {
        const statuses = await this.getAll();
        const status = statuses.find(s => s.nom === nom);
        return status || null;
    }

    static async create(status: Omit<Status, "id">): Promise<string> {
        const docRef = await addDoc(collection(db, StatusRepository.COLLECTION), status);
        return docRef.id;
    }

    static async update(id: string, data: Partial<Omit<Status, "id">>): Promise<void> {
        const ref = doc(db, StatusRepository.COLLECTION, id);
        await updateDoc(ref, data);
    }

    static async delete(id: string): Promise<void> {
        const ref = doc(db, StatusRepository.COLLECTION, id);
        await deleteDoc(ref);
    }
}
