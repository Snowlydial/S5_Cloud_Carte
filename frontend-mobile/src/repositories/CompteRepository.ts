
import {
  collection,
  addDoc,
  getDocs,
  getDoc,
  query,
  where,
  updateDoc,
  doc
} from "firebase/firestore";
import { Compte } from "../models/Compte";
import { db } from "@/firebase";


export class CompteRepository {
  static COLLECTION = "comptes";

  static async create(data: Omit<Compte, "idCompte">) {
    return await addDoc(collection(db, CompteRepository.COLLECTION), data);
  }

  static async findById(id: string): Promise<Compte | null> {
    const ref = doc(db, CompteRepository.COLLECTION, id);
    const snap = await getDoc(ref);
    return snap.exists()
      ? { idCompte: snap.id, ...(snap.data() as Omit<Compte, "idCompte">) }
      : null;
  }

  static async findByEmail(email: string): Promise<Compte | null> {
    const q = query(
      collection(db, CompteRepository.COLLECTION),
      where("email", "==", email)
    );

    const snap = await getDocs(q);
    if (snap.empty) return null;

    const doc = snap.docs[0];
    return { idCompte: doc.id, ...(doc.data() as Omit<Compte, "idCompte">) };
  }

  static async update(
    id: string,
    data: Partial<Omit<Compte, "idCompte">>
  ): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await updateDoc(ref, data);
  }
}
