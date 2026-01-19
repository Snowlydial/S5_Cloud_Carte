import { ref } from "vue";
import type { User } from "firebase/auth";
import { loginService, signinService } from "@/services/auth.service";
import { ApiResponse } from "@/types/apiResponse";
import L from 'leaflet'; // Assurez-vous d'avoir importé Leaflet pour le type
import { TypeSignalement } from "@/models/TypeSignalement";
import { Signalement } from "@/models/Signalement";
import { SignalementService } from "@/services/Signalement.service";



const typesSignalement = ref<TypeSignalement[] | null>(null);
const error = ref<string | null>(null);
const success = ref<string | null>(null);
const loading = ref(false);

const useSignalement = () => {
  const signaler = async (idTypeSignalement: number, coords: L.LatLngExpression) => {
    // Si vous avez besoin d'extraire les valeurs individuelles :
    // Dans le cas d'un tableau [-18.8792, 47.5079]
    const [lat, lng] = Array.isArray(coords) ? coords : [(coords as any).lat, (coords as any).lng];

    console.log(`Signalement type ${idTypeSignalement} à Lat: ${lat}, Lng: ${lng}`);

    try {
      // Votre logique d'appel API ici
          // const response: ApiResponse = await signalerService(idTypeSignalement, coords);
          const signalement:Signalement = {
            dateSignalement: new Date(),
            latitude: typeof lat === 'number' ? lat : lat!,
            longitude: typeof lng === 'number' ? lng : lng!,
            idCompte: 1,
          };
          console.log("Signalement créé :", signalement);

          SignalementService.create(signalement);

      // const response = await api.post('/signalements', { idTypeSignalement, lat, lng });
    } catch (err) {
      console.error("Erreur lors du signalement", err);
    }
  };
  return { signaler, loading, error , success };
};

export default useSignalement;
