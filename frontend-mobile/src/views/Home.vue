<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Accueil - Antananarivo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :scroll-y="false">
      <div class="main-wrapper">
        
        <div id="map" ref="mapContainer"></div>
        <ion-fab vertical="top" horizontal="end" slot="fixed" class="ion-margin fab-location">
        <ion-fab-button size="small" @click="getCurrentLocation" color="light">
          <ion-icon :icon="locateOutline"></ion-icon>
        </ion-fab-button>
      </ion-fab>

      <ion-fab vertical="top" horizontal="end" slot="fixed" class="ion-margin fab-logout">
        <ion-fab-button size="small" @click="handleLogout" color="light">
          <ion-icon :icon="logOutOutline"></ion-icon>
        </ion-fab-button>
      </ion-fab>
        <div class="form-container">
          <ion-list>
            <ion-item lines="none">
              <ion-label>
                <h2 v-if="form.lat !== null && form.lng !== null">
  📍 Position : {{ form.lat.toFixed(4) }}, {{ form.lng.toFixed(4) }}
</h2>
                <h2 v-else>Sélectionnez un point sur la carte</h2>
              </ion-label>
            </ion-item>

            <ion-item fill="outline" class="ion-margin-bottom">
              <ion-select v-model="form.type" label="Nature du danger" label-placement="floating">
                <ion-select-option v-for="t in typesSignalement" :key="t.idTypeSignalement" :value="t.idTypeSignalement">
                  {{ t.nom }}
                </ion-select-option>
              </ion-select>
            </ion-item>

            <ion-button expand="block" :disabled="!form.lat" @click="envoyerSignalement">
              Confirmer le signalement
            </ion-button>
          </ion-list>

          <ion-card v-if="recapView">
            <ion-card-header>
              <ion-card-title>Récapitulatif</ion-card-title>
            </ion-card-header>
            <ion-card-content>
              <ion-grid>
                <ion-row>
                  <ion-col size="6"><strong>Points :</strong></ion-col>
                  <ion-col size="6">{{ recapView.nbrPoint }}</ion-col>
                </ion-row>
                <ion-row>
                  <ion-col size="6"><strong>Surface totale :</strong></ion-col>
                  <ion-col size="6">{{ recapView.totalSurface }}</ion-col>
                </ion-row>
                <ion-row>
                  <ion-col size="6"><strong>Budget total :</strong></ion-col>
                  <ion-col size="6">{{ formatBudget(recapView.totalBudget) }}</ion-col>
                </ion-row>
                <ion-row>
                  <ion-col size="6"><strong>Avancement :</strong></ion-col>
                  <ion-col size="6">{{ recapView.avancement }} %</ion-col>
                </ion-row>
              </ion-grid>
            </ion-card-content>
          </ion-card>
        </div>
        
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
  IonList, IonItem, IonLabel, IonSelect, IonSelectOption, IonButton,
  IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonGrid, IonRow, IonCol
} from '@ionic/vue';
import { Geolocation } from '@capacitor/geolocation'; // Importation du plugin
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import useTypeSignalement from '@/composables/useTypeSignalement';
import useSignalement from '@/composables/useSignalement';
import { locateOutline, logOutOutline } from 'ionicons/icons';
import useAuth from '@/composables/useAuth';
import { useRouter } from 'vue-router';
import { Recap } from '@/types/Recap';
import { CompteService } from '@/services/Compte.service';

const { typesSignalement,  getListeTypeSignalement, error, success  } = useTypeSignalement();
const {signaler, loading  } = useSignalement();
const recap = ref<Recap | null>(null);
const { logout   } = useAuth();
const recapView = computed(() => recap.value);

const formatBudget = (value?: number) => {
  if (value === undefined || value === null) return "0.00";
  return value.toFixed(2);
};


// État réactif pour le formulaire
const form = ref({
  lat: null as number | null,
  lng: null as number | null,
  type: 'accident'
});

const typesSignalementListe = ref([]);

const callGetTypesSignalement = async () => {
  const result = await getListeTypeSignalement();
  typesSignalementListe.value = typesSignalementListe.value || [];
} 

callGetTypesSignalement();


let map: L.Map;
let marker: L.Marker | null = null;

// --- NOUVELLE FONCTION DE GÉOLOCALISATION ---
const getCurrentLocation = async () => {
  try {
    const coordinates = await Geolocation.getCurrentPosition({
      enableHighAccuracy: true
    });

    const { latitude, longitude } = coordinates.coords;
    const newPos = L.latLng(latitude, longitude);

    // 1. Centrer la carte
    map.setView(newPos, 16);

    // 2. Mettre à jour le formulaire
    form.value.lat = latitude;
    form.value.lng = longitude;

    // 3. Placer ou déplacer le marqueur
    if (marker) {
      marker.setLatLng(newPos);
    } else {
      marker = L.marker(newPos).addTo(map);
    }
  } catch (err) {
    console.error("Erreur de localisation", err);
    alert("Impossible de récupérer votre position. Vérifiez vos paramètres GPS.");
  }
};

const router = useRouter();

const handleLogout = async () => {
  try {
    await logout();
     router.push('/login');
  } catch (err) {
     router.push('/login');
    console.error("Erreur lors de la déconnexion", err);
  };
}

const envoyerSignalement = () => {
  if (form.value.lat === null || form.value.lng === null) {
    alert('Veuillez sélectionner une position sur la carte.');
    return;
  }
  // Construire coords au format Leaflet et appeler signaler
  const coords: L.LatLngExpression = [form.value.lat!, form.value.lng!];
  const idType =  form.value.type;
  console.log  ("Envoi du signalement :", {
    type: idType,
    coords: coords
  });
  signaler(idType, coords);
  // alert(`Signalement ${form.value.type} envoyé pour ${form.value.lat}, ${form.value.lng}`);
};

onMounted(async () => {
  const tanaCoords: L.LatLngExpression = [-18.8792, 47.5079];
  recap.value = await CompteService.getRecap  ();



  // Initialisation
  map = L.map('map').setView(tanaCoords, 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(map);

  // Événement clic
  map.on('click', (e: L.LeafletMouseEvent) => {
    form.value.lat = e.latlng.lat;
    form.value.lng = e.latlng.lng;

    // Mise à jour du marqueur visuel
    if (marker) {
      marker.setLatLng(e.latlng);
    } else {
      marker = L.marker(e.latlng).addTo(map);
    }
  });

  // Forcer le rendu
  setTimeout(() => {
    map.invalidateSize();
  }, 500);
  getCurrentLocation();
});
</script>

<style scoped>
/* Wrapper pour occuper tout l'écran disponible */
.main-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

#map {
  flex: 6; /* Prend 60% de l'espace disponible */
  width: 100%;
}

.form-container {
  flex: 4; /* Prend 40% de l'espace disponible */
  background: white;
  padding: 10px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  overflow-y: auto;
}

ion-fab {
  margin-top: 10px;
}

/* Position the first button normally */
.fab-location {
  top: 10px;
}

/* Push the logout button down (40px button height + 10px spacing) */
.fab-logout {
  top: 60px; 
}

#map { flex: 6; width: 100%; position: relative; }
.main-wrapper { display: flex; flex-direction: column; height: 100%; }
.form-container { flex: 4; background: white; padding: 10px; z-index: 10; }
</style>