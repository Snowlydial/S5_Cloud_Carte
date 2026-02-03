import {
  collection,
  addDoc,
  getDocs,
  query,
  where,
  updateDoc,
  doc,
  deleteDoc,
  orderBy
} from "firebase/firestore";
import { Signalement } from "../models/Signalement";
import { db } from "@/firebase";

export class SignalementRepository {
  static COLLECTION = "signalements";

  static async create(data: Omit<Signalement, "idSignalement">) {
    const result = await addDoc(collection(db, SignalementRepository.COLLECTION), data);
    await SignalementRepository.update(result.id, { firebaseId: result.id });
    return result;
  }

  static async findAll(): Promise<Signalement[]> {
    const q = query(
      collection(db, SignalementRepository.COLLECTION),
      orderBy("dateSignalement", "desc")
    );
    const snap = await getDocs(q);
    
    return snap.docs.map(doc => ({
      idSignalement: doc.id as any,
      ...(doc.data() as Omit<Signalement, "idSignalement">)
    }));
  }

  static async findById(id: string): Promise<Signalement | null> {
    const q = query(
      collection(db, SignalementRepository.COLLECTION),
      where("idSignalement", "==", id)
    );

    const snap = await getDocs(q);
    if (snap.empty) return null;

    const document = snap.docs[0];
    return {
      idSignalement: document.id as any,
      ...(document.data() as Omit<Signalement, "idSignalement">)
    };
  }

  static async findByIdCompte(idCompte : string): Promise<Signalement[]> {
    const q = query(
      collection(db, SignalementRepository.COLLECTION),
      where("idCompte", "==", idCompte),
      orderBy("dateSignalement", "desc")
    );

    const snap = await getDocs(q);
    return snap.docs.map(doc => ({
      idSignalement: doc.id as any,
      ...(doc.data() as Omit<Signalement, "idSignalement">)
    }));
  }

  static async update(
    id: string,
    data: Partial<Omit<Signalement, "idSignalement">>
  ): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await updateDoc(ref, data);
  }

  static async delete(id: string): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await deleteDoc(ref);
  }
}
