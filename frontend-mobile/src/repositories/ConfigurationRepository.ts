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
import { Configuration } from "../models/Configuration";
import { db } from "@/firebase";

export class ConfigurationRepository {
  static COLLECTION = "configuration";

  // Créer une nouvelle configuration
  static async create(data: Omit<Configuration, "idConfig">) {
    return await addDoc(collection(db, this.COLLECTION), data);
  }

  // Chercher par ID
  static async findById(id: string): Promise<Configuration | null> {
    const ref = doc(db, this.COLLECTION, id);
    const snap = await getDoc(ref);
    return snap.exists()
      ? { idConfig: snap.id, ...(snap.data() as Omit<Configuration, "idConfig">) }
      : null;
  }

  // Mettre à jour une configuration
  static async update(
    id: string,
    data: Partial<Omit<Configuration, "idConfig">>
  ): Promise<void> {
    const ref = doc(db, this.COLLECTION, id);
    await updateDoc(ref, data);
  }

  // Récupérer toutes les configurations
  static async findAll(): Promise<Configuration[]> {
    const snap = await getDocs(collection(db, this.COLLECTION));
    return snap.docs.map(d => ({
      idConfig: d.id,
      ...(d.data() as Omit<Configuration, "idConfig">)
    }));
  }

  // Si tu veux chercher par tentative_max ou m2_forfaitaire
  static async findByTentativeMax(tentative_max: number): Promise<Configuration[]> {
    const q = query(
      collection(db, this.COLLECTION),
      where("tentative_max", "==", tentative_max)
    );
    const snap = await getDocs(q);
    return snap.docs.map(d => ({
      idConfig: d.id,
      ...(d.data() as Omit<Configuration, "idConfig">)
    }));
  }
}
