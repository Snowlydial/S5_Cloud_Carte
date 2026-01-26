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
          <div class="fab-container fab-location">
          <span class="fab-label">Ma position</span>
          <ion-fab-button size="small" @click="getCurrentLocation" color="light">
            <ion-icon :icon="locateOutline"></ion-icon>
          </ion-fab-button>
        </div>

        <div class="fab-container fab-logout">
          <span class="fab-label">Déconnexion</span>
          <ion-fab-button size="small" @click="handleLogout" color="light">
            <ion-icon :icon="logOutOutline"></ion-icon>
          </ion-fab-button>
        </div>

        <div class="fab-container fab-filter">
          <span class="fab-label">Mes signalements</span>
          <ion-fab-button size="small" @click="filterMySignalement" color="light">
            <ion-icon :icon="filterOutline"></ion-icon>
          </ion-fab-button>
        </div>
      
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
                  <ion-col size="6">{{ format(recapView.totalSurface) }}</ion-col>
                </ion-row>
                <ion-row>
                  <ion-col size="6"><strong>Budget total :</strong></ion-col>
                  <ion-col size="6">{{ format(recapView.totalBudget) }}</ion-col>
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
  const iconConfigs: Record<string, { icon: string, color: string }> = {
  '2': { icon: '🚧', color: '#e67e22' }, // Route endommagée - Orange
  '7': { icon: '⚠️', color: '#e74c3c' }, // Conducteur en danger - Rouge
  '3': { icon: '💡', color: '#f1c40f' }, // Éclairage défaillant - Jaune
  '1': { icon: '🕳️', color: '#7f8c8d' }, // Nid de poule - Gris
  '5': { icon: '🪵', color: '#a04000' }, // Débris - Marron
  '6': { icon: '🚗', color: '#c0392b' }, // Accident - Rouge foncé
  '4': { icon: '🚫', color: '#2980b9' }, // Signalisation - Bleu
};

// Icône par défaut
const defaultIconConfig = { icon: '📍', color: '#2ecc71' };

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
import { filterOutline, locateOutline, logOutOutline } from 'ionicons/icons';
import useAuth from '@/composables/useAuth';
import { useRouter } from 'vue-router';
import { Recap } from '@/types/Recap';
import { CompteService } from '@/services/Compte.service';


const recap = ref<Recap | null>(null);
const { logout   } = useAuth();
const recapView = computed(() => recap.value);

const format = (value?: number) => {
  if (value === undefined || value === null) return "0.00";
  return value.toFixed(2);
};
import { Signalement } from '@/models/Signalement';
import { TypeSignalementService } from '@/services/TypeSignalement.service';

const { typesSignalement,  getListeTypeSignalement, error, success} = useTypeSignalement();
const {signaler, loading , getAllSignalements, listeSignalement, getAllSignalementsMine} = useSignalement();

// getAllSignalements();
const listeSignalementEffectif = ref<Signalement[]>([]);

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

const filterMySignalement = async () => {
  try {
    await getAllSignalementsMine();

    console.log("Filtre applique avec succes");
      renderSignalementMarkers(listeSignalement.value);
    
  } catch (err) {
    console.error("Erreur lors de l'obtention avec filtres", err);
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
  loadMapData();
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

  await loadMapData();

  // Forcer le rendu
  setTimeout(() => {
    map.invalidateSize();
  }, 500);
  getCurrentLocation();
});
// Fonction pour ajouter les marqueurs
const markersLayer = L.layerGroup(); // Pour éviter l'empilement

const renderSignalementMarkers = (signalements: Signalement[]) => {
  console.log("nombre signalements = " + signalements.length);
  // console.log("signalements = " + JSON.stringify(signalements));
  markersLayer.clearLayers(); 

  signalements.forEach(async (sig) => {
    if (sig.latitude && sig.longitude) {
      // On récupère l'idTypeSignalement du modèle
      const typeId = String(sig.idTypeSignalement);
      const typeSignalement = await TypeSignalementService.getById (typeId);
      const image =   sig.idimage?.toString() || '';


      console.log ("typeSignalement = ", typeSignalement);
      const  compte = await CompteService.getById (sig.idCompte  || '' );
      const markerInstance = L.marker([sig.latitude, sig.longitude], { icon: createCustomIcon(image) })
        .addTo(markersLayer);

      markerInstance
        .bindTooltip(
          `
            <div style="font-family: sans-serif;">
              <strong>Signalement #${sig.idSignalement}</strong><br>
              Type: ${typeSignalement?.nom}<br>
              Posté par: ${compte?.email}
            </div>
          `,
          { direction: "top", sticky: true, opacity: 0.9 }
        )
        .on("mouseover", () => {
          markerInstance.openTooltip();
        })
        .on("mouseout", () => {
          markerInstance.closeTooltip();
        });
    }
  });
  markersLayer.addTo(map);
};

const createCustomIcon = (typeId: string) => {
  const config = iconConfigs[typeId] || defaultIconConfig;
  
  return L.divIcon({
    className: 'custom-marker',
    html: `
      <div style="
        background-color: ${config.color};
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50% 50% 50% 0;
        transform: rotate(-45deg);
        border: 3px solid white;
        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
      ">
        <span style="transform: rotate(45deg); font-size: 20px;">${config.icon}</span>
      </div>
    `,
    iconSize: [40, 40],
    iconAnchor: [20, 40], // Pointe du marqueur
    popupAnchor: [0, -40]
  });
};

// Fonction pour charger toutes les données et les afficher sur la carte
const loadMapData = async () => {
  try {
    // On attend que les deux appels API soient terminés
    await Promise.all([
      getAllSignalements(),
      getListeTypeSignalement()
    ]);
    listeSignalementEffectif.value = listeSignalement.value;
    // Une fois les données reçues, on dessine les marqueurs
    if (listeSignalement.value && listeSignalement.value.length > 0) {
      console.log("Signalements chargés :", listeSignalement.value);
      renderSignalementMarkers(listeSignalement.value);
    }
  } catch (err) {
    console.error("Erreur lors du chargement des données de la carte :", err);
    // Optionnel : afficher une alerte utilisateur ici
  }
};
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

.fab-container {
  position: absolute;
  right: 16px;
  display: flex;
  align-items: center;
  z-index: 1000; /* Pour passer au dessus de la carte Leaflet */
}

.fab-label {
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 8px;
  border-radius: 4px;
  margin-right: 10px;
  font-size: 0.8rem;
  font-weight: bold;
  color: #333;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  pointer-events: none; /* Le clic traverse le texte vers le bouton si besoin */
  opacity: 0.8;
  transition: opacity 0.2s;
}

.fab-container:hover .fab-label {
  opacity: 1;
}

ion-fab {
  margin-top: 10px;
}

/* Position the first button normally */
.fab-location { top: 10px; }
.fab-logout   { top: 60px; }
.fab-filter   { top: 110px; }


#map { flex: 6; width: 100%; position: relative; }
.main-wrapper { display: flex; flex-direction: column; height: 100%; }
.form-container { flex: 4; background: white; padding: 10px; z-index: 10; overflow-y: auto;}

:deep(.custom-marker) {
  background: transparent;
  border: none;
}


</style>