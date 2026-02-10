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
                <ion-item fill="outline" class="neo-input">
                  <ion-select v-model="form.type" placeholder="Sélectionner">
                    <ion-select-option v-for="t in typesSignalement" :key="t.idTypeSignalement" :value="t.idTypeSignalement">
                      {{ t.nom }}
                    </ion-select-option>
                  </ion-select>
                </ion-item>
              </div>

              <div class="form-group">
                <label class="form-label">Description</label>
                <ion-item fill="outline" class="neo-input">
                  <ion-input 
                    :value="form.description" 
                    @ionInput="form.description = ($event.target as unknown as HTMLInputElement).value || ''"
                    placeholder="Décrivez le signalement">
                  </ion-input>
                </ion-item>
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
  IonItem, IonSelect, IonSelectOption, IonButton, IonInput,
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
//     const image = await Camera.getPhoto({
//       quality: 90,
//       allowEditing: false,
//       resultType: CameraResultType.Uri,
//       source: CameraSource.Prompt // Propose Galerie ou Appareil photo
//     });
//     selectedPhotos.value.push(image);
//   } catch (err) {
//     console.log("Utilisateur a annulé la sélection");
//   }
// };

const removePhoto = (index: number) => {
  selectedPhotos.value.splice(index, 1);
};

  // MODAL IMAGE


const recap = ref<Recap | null>(null);
const { logout   } = useAuth();
const recapView = computed(() => recap.value);

const format = (value?: number) => {
  if (value === undefined || value === null) return "0.00";
  return value.toFixed(2);
};

import { Signalement } from '@/models/Signalement';
import { TypeSignalementService } from '@/services/TypeSignalement.service';
import { PhotoService } from '@/services/Photo.service';
import { SupabaseService } from '@/services/supabase.service';

const { typesSignalement,  getListeTypeSignalement, error, success} = useTypeSignalement();
const {signaler, loading , getAllSignalements, listeSignalement, getAllSignalementsMine} = useSignalement();

const {listeSignalementProbleme, fetchAllData} = useSignalementProbleme();
// getAllSignalements();
const listeSignalementEffectif = ref<Signalement[]>([]);

// État réactif pour le formulaire
const form = ref({
  lat: null as number | null,
  lng: null as number | null,
  type: 'accident',
  description: ''
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
  isPhotoModalOpen.value = true;
  // Construire coords au format Leaflet et appeler signaler
  // const coords: L.LatLngExpression = [form.value.lat!, form.value.lng!];
  // const idType =  form.value.type;
  // console.log  ("Envoi du signalement :", {
  //   type: idType,
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
                ⚠️ Le signalement n’a pas encore été traité
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