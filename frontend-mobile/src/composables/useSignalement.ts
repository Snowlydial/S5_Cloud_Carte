import { ref } from "vue";
import { getAuth, type User } from "firebase/auth";
import { loginService, signinService } from "@/services/auth.service";
import { ApiResponse } from "@/types/apiResponse";
import L from 'leaflet'; // Assurez-vous d'avoir importé Leaflet pour le type
import { TypeSignalement } from "@/models/TypeSignalement";
import { Signalement } from "@/models/Signalement";
import { SignalementService } from "@/services/Signalement.service";
import { Util } from "@/utils/util";
import { FirebaseImageService } from "@/services/FirebaseImage.service";

const typesSignalement = ref<TypeSignalement[] | null>(null);
const error = ref<string | null>(null);
const success = ref<string | null>(null);
const loading = ref(false);
const listeSignalement = ref<Signalement[]>([]);
const compteId = ref<string | null>(null);
const useSignalement = () => {

  // Rajouter les problemes
  const getAllSignalements = async () => {
    try {
      const result:Signalement[] = await SignalementService.getAll();
      listeSignalement.value = result;

    } catch (error) {
      console.error("Erreur lors de la récupération des signalements", error);
    }
  }

  const getAllSignalementsMine = async () => {
    try {
      const result:Signalement[] = await SignalementService.getAll();
      const myId = await Util.getCompteId();
      console.log("Mon ID compte :", myId);
      listeSignalement.value = result.filter(sig => String(sig.idCompte) === String(myId));
      console.log("Mes signalements apres filtres:", listeSignalement.value.length);
      // listeSignalement.value = result;
    } catch (error) {
      console.error("Erreur lors de la récupération des signalements", error);
    }
  }

 
  const signaler = async (idTypeSignalement: string, coords: L.LatLngExpression, selectedPhotos: any[]) => {
    const [lat, lng] = Array.isArray(coords) ? coords : [(coords as any).lat, (coords as any).lng];
    loading.value = true;

    try {
        const auth = getAuth();
        const currentUser = auth.currentUser;
        
        if (!currentUser) throw new Error("Utilisateur non authentifié");

        const myId = await Util.getCompteId();
        compteId.value = myId;
        console.log ("ID Compte pour le signalement :", compteId.value);

        // --- RECHERCHE DE L'ID IMAGE ---
        // On cherche dans la liste des types celui qui correspond à l'ID sélectionné
        let imageId = null;
        if(typesSignalement.value === null) {
        } else {
          const typeSelectionne = typesSignalement.value.find(
            t => String(t.idTypeSignalement) === String(idTypeSignalement)
          );
          imageId = typeSelectionne?.idimage || null;
        }


        // Si trouvé, on prend son idimage, sinon on peut mettre une valeur par défaut ou null
        const signalement: Signalement = {
            dateSignalement: new Date(),
            latitude: Number(lat),
            longitude: Number(lng),
            idCompte: compteId.value,
            idTypeSignalement: idTypeSignalement,
        };

        const base64Images = await Promise.all(
        selectedPhotos.map(p => FirebaseImageService.processAndCompress(p))
    );

        console.log("Signalement prêt à l'envoi :", signalement);
        await SignalementService.create(signalement, base64Images);
        success.value = "Signalement envoyé avec succès";

    } catch (err) {
        console.error("Erreur lors du signalement", err);
        error.value = "Échec de l'envoi";
    } finally {
        loading.value = false;
    }
  }
  return { signaler, loading, error , success, getAllSignalements, listeSignalement, getAllSignalementsMine};
};

export default useSignalement;
