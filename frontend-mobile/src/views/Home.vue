<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte d'Antananarivo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :scroll-y="false">
      <div class="main-wrapper">
        <!-- Fullscreen Map -->
        <div id="map" ref="mapContainer"></div>
        
        <!-- FAB Buttons with Neobrutalism styling -->
        <div class="neo-fab-container fab-location">
          <span class="neo-fab-label">Position</span>
          <button class="neo-fab-button" @click="getCurrentLocation">
            <ion-icon :icon="locateOutline"></ion-icon>
          </button>
        </div>

        <div class="neo-fab-container fab-logout">
          <span class="neo-fab-label">Déconnexion</span>
          <button class="neo-fab-button neo-fab-danger" @click="handleLogout">
            <ion-icon :icon="logOutOutline"></ion-icon>
          </button>
        </div>

        <div class="neo-fab-container fab-filter">
          <span class="neo-fab-label">Mes signalements</span>
          <button class="neo-fab-button neo-fab-info" @click="filterMySignalement">
            <ion-icon :icon="filterOutline"></ion-icon>
          </button>
        </div>

        <!-- Recap FAB Button -->
        <div v-if="recapView" class="neo-fab-container fab-recap">
          <span class="neo-fab-label">Récapitulatif</span>
          <button class="neo-fab-button neo-fab-secondary" @click="isRecapModalOpen = true">
            <ion-icon :icon="statsChartOutline"></ion-icon>
          </button>
        </div>

        <!-- Bottom Sheet -->
        <div class="bottom-sheet" :class="{ 'sheet-expanded': isSheetExpanded }">
          <!-- Handle to drag/toggle -->
          <div class="sheet-handle" @click="isSheetExpanded = !isSheetExpanded">
            <div class="handle-bar"></div>
            <span class="handle-text">{{ isSheetExpanded ? 'Réduire' : 'Nouveau signalement' }}</span>
            <ion-icon :icon="isSheetExpanded ? chevronDownOutline : chevronUpOutline" class="handle-icon"></ion-icon>
          </div>

          <!-- Sheet Content (scrollable) -->
          <div class="sheet-content">
            <!-- Position Badge -->
            <div class="neo-position-badge">
              <span class="badge-label">Position sélectionnée</span>
              <span class="badge-value" v-if="form.lat !== null && form.lng !== null">
                📍 {{ form.lat.toFixed(4) }}, {{ form.lng.toFixed(4) }}
              </span>
              <span class="badge-value badge-warning" v-else>
                Touchez la carte pour sélectionner
              </span>
            </div>

            <!-- Form Card -->
            <div class="neo-form-card">
              <div class="form-group">
                <label class="form-label">Nature du danger</label>
                <select v-model="form.type" class="neo-select">
                  <option value="" disabled selected>Sélectionner</option>
                  <option v-for="t in typesSignalement" :key="t.idTypeSignalement" :value="t.idTypeSignalement">
                    {{ t.nom }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">Description</label>
                <input 
                  v-model="form.description"
                  type="text"
                  class="neo-input-text"
                  placeholder="Décrivez le signalement"
                />
              </div>
              
              <ion-button expand="block" :disabled="!form.lat" @click="envoyerSignalement" class="neo-submit-btn">
                Confirmer le signalement
              </ion-button>
            </div>
          </div>
        </div>

        <!-- Recap Modal -->
        <ion-modal :is-open="isRecapModalOpen" @didDismiss="isRecapModalOpen = false">
          <ion-header>
            <ion-toolbar>
              <ion-title>Récapitulatif global</ion-title>
              <ion-buttons slot="end">
                <ion-button @click="isRecapModalOpen = false">Fermer</ion-button>
              </ion-buttons>
            </ion-toolbar>
          </ion-header>
          <ion-content class="ion-padding recap-modal-content">
            <div v-if="recapView" class="recap-modal-grid">
              <div class="recap-modal-item">
                <span class="recap-modal-label">Points signalés</span>
                <span class="recap-modal-value">{{ recapView.nbrPoint }}</span>
              </div>
              <div class="recap-modal-item">
                <span class="recap-modal-label">Surface totale</span>
                <span class="recap-modal-value">{{ format(recapView.totalSurface) }} m²</span>
              </div>
              <div class="recap-modal-item recap-modal-highlight">
                <span class="recap-modal-label">Budget total</span>
                <span class="recap-modal-value">{{ format(recapView.totalBudget) }} AR</span>
              </div>
              <div class="recap-modal-item recap-modal-success">
                <span class="recap-modal-label">Avancement</span>
                <span class="recap-modal-value">{{ recapView.avancement }} %</span>
              </div>
            </div>
          </ion-content>
        </ion-modal>

        <!-- Photo Modal -->
        <ion-modal :is-open="isPhotoModalOpen" @didDismiss="isPhotoModalOpen = false">
          <ion-header>
            <ion-toolbar>
              <ion-title>Ajouter des photos</ion-title>
              <ion-buttons slot="end">
                <ion-button @click="isPhotoModalOpen = false">Fermer</ion-button>
              </ion-buttons>
            </ion-toolbar>
          </ion-header>
          <ion-content class="ion-padding">
            <div class="photo-grid">
              <div v-for="(photo, index) in selectedPhotos" :key="index" class="photo-item">
                <img :src="photo.webPath" />
                <button class="neo-photo-delete" @click="removePhoto(index)">
                  <ion-icon :icon="trashOutline"></ion-icon>
                </button>
              </div>
              <div class="add-photo-btn" @click="takePhoto">
                <ion-icon :icon="cameraOutline" size="large"></ion-icon>
                <p>Ajouter une photo</p>
              </div>
            </div>
            
            <ion-button expand="block" class="ion-margin-top" @click="finaliserSignalement">
              Envoyer le signalement
            </ion-button>
          </ion-content>
        </ion-modal>
        
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
  IonButton,
  IonModal, IonButtons, IonIcon
} from '@ionic/vue';
import { Geolocation } from '@capacitor/geolocation'; // Importation du plugin
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import useTypeSignalement from '@/composables/useTypeSignalement';
import useSignalement from '@/composables/useSignalement';
import { filterOutline, locateOutline, logOutOutline, chevronUpOutline, chevronDownOutline, statsChartOutline } from 'ionicons/icons';
import useAuth from '@/composables/useAuth';
import { useRouter } from 'vue-router';
import { Recap } from '@/types/Recap';
import { CompteService } from '@/services/Compte.service';

import { SignalementProbleme } from "@/models/SignalementProbleme";

import useSignalementProbleme from '@/composables/useSignalementProbleme' 

import { cameraOutline, trashOutline } from 'ionicons/icons';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';

// Bottom sheet state
const isSheetExpanded = ref(false);

// Recap modal state
const isRecapModalOpen = ref(false);

// MODAL IMAGE
const isPhotoModalOpen = ref(false);
const selectedPhotos = ref<any[]>([]);

const takePhoto = async () => {
  const photo = await PhotoService.selectFromGallery();
  if (photo) {
    // On ajoute l'objet photo à notre liste
    selectedPhotos.value.push(photo);
  }
};

// NOUVEAU : Fonction pour ouvrir la caméra/galerie
// const takePhoto = async () => {
//   try {
//     const photo = await Camera.getPhoto({
//       quality: 90,
//       allowEditing: false,
//       resultType: CameraResultType.Uri, // ou DataUrl si vous voulez la base64
//       source: CameraSource.Camera      // ou Prompt pour choisir Camera/Galerie
//     });

//     // photo.webPath contient l'URL de l'image
//     selectedPhotos.value.push(photo);
//   } catch (error) {
//     console.error("Erreur lors de la prise de photo:", error);
//   }
// };

const removePhoto = (index: number) => {
  selectedPhotos.value.splice(index, 1);
};

const { typesSignalement, getListeTypeSignalement } = useTypeSignalement();
const { 
  signaler, 
  getAllSignalements, 
  listeSignalement, 
  mySignalement 
} = useSignalement();

import { PhotoService } from '@/services/Photo.service';
import { TypeSignalementService } from '@/services/TypeSignalement.service';

const { 
  listeSignalementProbleme, 
  fetchAllData 
} = useSignalementProbleme();

const { logout, currentUser } = useAuth();
const router = useRouter();

// Form state
const form = ref({
  type: "",
  lat: null as number | null,
  lng: null as number | null,
  description: ""
});

let map: L.Map;
let marker: L.Marker | null = null;
const mapContainer = ref();
const loading = ref(false);
const recap = ref<Recap>();

const recapView = computed(() => {
  return recap.value || null;
});

const format = (num : number) => {
  return Math.round(num);
}

const listeSignalementEffectif = ref<SignalementProbleme[]>([]);
const filterMySignalement = async () => {
  if(listeSignalementEffectif.value.length <= 0 || listeSignalementEffectif.value === listeSignalementProbleme.value){
    const mesSig = await mySignalement(currentUser.value?.email as string);
    console.log("Mes signalements :", mesSig);
    renderSignalementMarkers(mesSig as SignalementProbleme[]);
    listeSignalementEffectif.value = mesSig as SignalementProbleme[];
  } else {
    renderSignalementMarkers(listeSignalementProbleme.value);
    listeSignalementEffectif.value = listeSignalementProbleme.value;
  }
};


// Fonction pour récupérer la position actuelle
const getCurrentLocation = async () => {
  try {
    // 1️⃣ Demande de permission si besoin
    const permission = await Geolocation.requestPermissions();
    if (permission.location !== 'granted') {
      console.warn("Permission de localisation refusée.");
      return;
    }

    // 2️⃣ Récupération de la position
    const position = await Geolocation.getCurrentPosition();
    const { latitude, longitude } = position.coords;

    console.log("✅ Position actuelle :", latitude, longitude);

    // 3️⃣ Centrer la carte
    map.setView([latitude, longitude], 14);

    // 4️⃣ Ajouter un marqueur à cette position
    if (marker) {
      marker.setLatLng([latitude, longitude]);
    } else {
      marker = L.marker([latitude, longitude]).addTo(map);
    }

    // 5️⃣ Pré-remplir le formulaire (optionnel)
    form.value.lat = latitude;
    form.value.lng = longitude;

  } catch (error) {
    console.error("Erreur de géolocalisation :", error);
  }
};

const handleLogout = async () => {
  await logout();
  router.push('/login');
};

const envoyerSignalement = () => {
  // Ouvrir la modale d'ajout de photos
  isPhotoModalOpen.value = true;
  
  // Ancien code pour envoi direct :
  // const coords: L.LatLngExpression = [form.value.lat!, form.value.lng!];
  // const idType = form.value.type;
  // const desc = form.value.description;
  // await signaler(idType, coords, {
  //   coords: coords
  // });
  // signaler(idType, coords);
  // loadMapData();
  // alert(`Signalement ${form.value.type} envoyé pour ${form.value.lat}, ${form.value.lng}`);
};

const finaliserSignalement = async () => {
  loading.value = true; // Si vous avez un état de chargement
    
    // 1. Envoyer les images à Supabase et récupérer les URLs
    let imageUrls: string[] = [];
    if (selectedPhotos.value.length > 0) {
      // imageUrls = await SupabaseService.uploadCapacitorPhotos(selectedPhotos.value);
    }
    
  const coords: L.LatLngExpression = [form.value.lat!, form.value.lng!];
  const idType = form.value.type;
  const desc = form.value.description;
  
  // Ici, vous devrez probablement convertir vos images en Base64 ou FormData 
  // pour les envoyer à votre API via votre composable 'signaler'
  console.log("Envoi final avec", selectedPhotos.value.length, "photos");
  
  await signaler(idType, coords, selectedPhotos.value, desc); // Modifiez votre composable pour accepter les photos
  
  isPhotoModalOpen.value = false;
  selectedPhotos.value = []; // Reset
  loadMapData();
};

onMounted(async () => {
  const tanaCoords: L.LatLngExpression = [-18.8792, 47.5079];
  recap.value = await CompteService.getRecap();

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
  await getListeTypeSignalement();

  // Forcer le rendu
  setTimeout(() => {
    map.invalidateSize();
  }, 500);
  getCurrentLocation();
});

// Fonction pour ajouter les marqueurs
const markersLayer = L.layerGroup(); // Pour éviter l'empilement

const renderSignalementMarkers = async (signalements: SignalementProbleme[]) => {
  markersLayer.clearLayers();

  signalements.forEach(async (sig) => {
    if (!sig.latitude || !sig.longitude) return;

    const hasProbleme = !!sig.idProbleme;
    const typeSingnalement = await TypeSignalementService.getById(sig.idTypeSignalement as string);

    const markerInstance = L.marker(
      [sig.latitude, sig.longitude],
      {
        icon: createCustomIcon(
          String(typeSingnalement?.idimage ?? ''),
          hasProbleme
        )
      }
    ).addTo(markersLayer);

    const tooltipHtml = `
      <div style="font-family: sans-serif; min-width: 240px;">
        <strong>Signalement #${sig.idSignalement}</strong><br>

        <strong>Type :</strong> ${sig.typeNom ?? "—"}<br>

        <hr style="margin:6px 0"/>

        ${
          hasProbleme
            ? `
              <strong>🛠 Problème associé</strong><br>
              Surface : ${sig.surfaceM2 ?? 0} m²<br>
              Budget : ${sig.budget ?? 0} Ar<br>
              Entreprise : ${sig.nomEntreprise ?? "Non précisée"}<br>
              Statut : ${sig.statusActuel ?? "—"}<br>
              <small>
                ${sig.statusDate
                  ? new Date(sig.statusDate).toLocaleDateString()
                  : ""}
              </small>
            `
            : `
              <em style="color:#e67e22;">
                ⚠️ Le signalement n'a pas encore été traité
              </em>
            `
        }
      </div>
    `;

    markerInstance
      .bindTooltip(tooltipHtml, {
        direction: "top",
        sticky: true,
        opacity: 0.95
      })
      .on("mouseover", () => markerInstance.openTooltip())
      .on("mouseout", () => markerInstance.closeTooltip());
  });

  markersLayer.addTo(map);
};


const createCustomIcon = (typeId: string, hasProbleme: boolean) => {
  const config = iconConfigs[typeId] || defaultIconConfig;

  console.log ("Création icône pour typeId:", typeId, "avec config:", config, "et hasProbleme:", hasProbleme);

  

  return L.divIcon({
    className: 'custom-marker',
    html: `
      <div style="
        position: relative;
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
        <span style="transform: rotate(45deg); font-size: 20px;">
          ${config.icon}
        </span>

        ${
          hasProbleme
            ? `
              <span style="
                position: absolute;
                top: -6px;
                right: -6px;
                background: #e74c3c;
                color: white;
                font-size: 12px;
                font-weight: bold;
                width: 18px;
                height: 18px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                transform: rotate(45deg);
              ">!</span>
            `
            : ''
        }
      </div>
    `,
    iconSize: [40, 40],
    iconAnchor: [20, 40],
    popupAnchor: [0, -40]
  });
};


// Fonction pour charger toutes les données et les afficher sur la carte
// const loadMapData = async () => {
//   try {
//     // On attend que les deux appels API soient terminés
//     await Promise.all([
//       getAllSignalements(),
//       getListeTypeSignalement()
//     ]);
//     listeSignalementEffectif.value = listeSignalement.value;
//     // Une fois les données reçues, on dessine les marqueurs
//     if (listeSignalement.value && listeSignalement.value.length > 0) {
//       console.log("Signalements chargés :", listeSignalement.value);
//       renderSignalementMarkers(listeSignalement.value);
//     }
//   } catch (err) {
//     console.error("Erreur lors du chargement des données de la carte :", err);
//     // Optionnel : afficher une alerte utilisateur ici
//   }
// };

const loadMapData = async () => {
  try {
    await fetchAllData();

    if (listeSignalementProbleme.value.length > 0) {
      renderSignalementMarkers(listeSignalementProbleme.value);
    }
  } catch (err) {
    console.error("Erreur chargement signalements avec problèmes", err);
  }
};

</script>

<style scoped src="./Home.css"></style>