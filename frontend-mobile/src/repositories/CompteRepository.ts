
import {
  collection,
  addDoc,
  getDocs,
  query,
  where,
  updateDoc,
  doc
} from "firebase/firestore";
import { Compte } from "../models/Compte";
import { db } from "@/firebase";


export class CompteRepository {
  static COLLECTION = "comptes";

  static async create(data: Omit<Compte, "id">) {
    return await addDoc(collection(db, CompteRepository.COLLECTION), data);
  }

  static async findByEmail(email: string): Promise<Compte | null> {
    const q = query(
      collection(db, CompteRepository.COLLECTION),
      where("email", "==", email)
    );

    const snap = await getDocs(q);
    if (snap.empty) return null;

    const doc = snap.docs[0];
    return { id: doc.id, ...(doc.data() as Omit<Compte, "id">) };
  }

  static async update(
    id: string,
    data: Partial<Omit<Compte, "id">>
  ): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await updateDoc(ref, data);
  }
}
