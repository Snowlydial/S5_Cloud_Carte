import {
  collection,
  addDoc,
  getDocs,
  query,
  where,
  updateDoc,
  doc,
  deleteDoc,
  getDoc
} from "firebase/firestore";
import { TypeSignalement } from "../models/TypeSignalement";
import { db } from "@/firebase";


export class TypeSignalementRepository {
  static COLLECTION = "type_signalement";

  static async create(data: Omit<TypeSignalement, "idTypeSignalement">) {
    const result = await addDoc(collection(db, TypeSignalementRepository.COLLECTION), data);
    await TypeSignalementRepository.update(result.id, { firebaseId: result.id });
    return result;
  }

  static async findAll(): Promise<TypeSignalement[]> {
    const q = query(collection(db, TypeSignalementRepository.COLLECTION));
    const snap = await getDocs(q);

    return snap.docs.map(doc => ({
      idTypeSignalement: doc.id as any,
      ...(doc.data() as Omit<TypeSignalement, "idTypeSignalement">)
    }));
  }

  static async findById(id: string): Promise<TypeSignalement | null> {
    const ref = doc(db, TypeSignalementRepository.COLLECTION, id);
    const snap = await getDoc(ref);

    if (!snap.exists()) return null;

    return {
      idTypeSignalement: snap.id as any,
      ...(snap.data() as Omit<TypeSignalement, "idTypeSignalement">)
    };
  }

  static async findByNom(nom: string): Promise<TypeSignalement | null> {
    const q = query(
      collection(db, TypeSignalementRepository.COLLECTION),
      where("nom", "==", nom)
    );

    const snap = await getDocs(q);
    if (snap.empty) return null;

    const document = snap.docs[0];
    return {
      idTypeSignalement: document.id as any,
      ...(document.data() as Omit<TypeSignalement, "idTypeSignalement">)
    };
  }

  static async update(
    id: string,
    data: Partial<Omit<TypeSignalement, "idTypeSignalement">>
  ): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await updateDoc(ref, data);
  }

  static async delete(id: string): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await deleteDoc(ref);
  }
}
