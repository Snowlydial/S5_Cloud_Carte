import { ref } from "vue";
import type { User } from "firebase/auth";
import { loginService, signinService } from "@/services/auth.service";
import { ApiResponse } from "@/types/apiResponse";
import { getErrorMessage } from "@/utils/getErrorMessage";
import { getSuccessMessage } from "@/utils/getSuccessMessage";
import L from 'leaflet'; // Assurez-vous d'avoir importé Leaflet pour le type

export interface TypeSignalement {
    idTypeSignalement: number;
    nom: string;
}

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

      // const response = await api.post('/signalements', { idTypeSignalement, lat, lng });
    } catch (err) {
      console.error("Erreur lors du signalement", err);
    }
  };
  return { signaler, loading, error , success };
};

export default useSignalement;
