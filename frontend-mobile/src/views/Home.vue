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

        <div class="form-container">
          <ion-list>
            <ion-item lines="none">
              <ion-label>
                <h2 v-if="form.lat">📍 Position : {{ form.lat.toFixed(4) }}, {{ form.lng.toFixed(4) }}</h2>
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
        </div>
        
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
  IonList, IonItem, IonLabel, IonSelect, IonSelectOption, IonButton 
} from '@ionic/vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import useTypeSignalement from '@/composables/useTypeSignalement';

const { typesSignalement,  getListeTypeSignalement, error, success  } = useTypeSignalement();

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

const envoyerSignalement = () => {
  alert(`Signalement ${form.value.type} envoyé pour ${form.value.lat}, ${form.value.lng}`);
};

onMounted(() => {
  const tanaCoords: L.LatLngExpression = [-18.8792, 47.5079];

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
</style>