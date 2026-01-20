import { collection, deleteDoc, doc, getDocs } from "firebase/firestore";
import { db } from "../firebase";
import { auth } from "../firebase";
// import { alertController } from "@ionic/vue";

export class Util {
    static async clearCollection(collectionName: string) {
        const colRef = collection(db, collectionName); // référence correcte
        const snapshot = await getDocs(colRef);

        const promises = snapshot.docs.map(d =>
            deleteDoc(doc(db, collectionName, d.id))
        );

        await Promise.all(promises);
        console.log(`✅ Collection '${collectionName}' vidée !`);
    }



    static async checkSession() {
        // const SESSION_DURATION_MS = 4 * 60 * 60 * 1000; // 4 heures
        const SESSION_DURATION_MS = 5 * 1000; // 10s
        const loginTime = Number(localStorage.getItem("loginTime"));
        if (!loginTime) return false; // pas de session

        const expired = Date.now() - loginTime > SESSION_DURATION_MS;
        console.log("session check:", expired);
        if (expired) {
            auth.signOut();
            localStorage.removeItem("loginTime");
            localStorage.setItem ("expired", "true");
            // const alert = await alertController.create({
            //     header: 'Échec de connexion',
            //     message: 'Votre session a expiré. Veuillez vous reconnecter.',
            //     buttons: ['OK'],
            // });
            // await alert.present();
            return false;
        }
        return true;
    }

}
