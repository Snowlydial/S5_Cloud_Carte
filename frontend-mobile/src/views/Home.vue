<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Accueil - Antananarivo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div id="map" ref="mapContainer"></div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent } from '@ionic/vue';
import L from 'leaflet';

// Import des styles CSS de Leaflet (Indispensable !)
import 'leaflet/dist/leaflet.css';

const mapContainer = ref<HTMLElement | null>(null);
let map: L.Map;

onMounted(() => {
  // Coordonnées de Antananarivo
  const tanaCoords: L.LatLngExpression = [-18.8792, 47.5079];

  // Initialisation de la carte
  map = L.map('map').setView(tanaCoords, 13);

  // Ajout de la couche de tuiles (OpenStreetMap)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(map);

  // Ajout d'un marqueur sur Antananarivo
  L.marker(tanaCoords).addTo(map)
    .bindPopup('Bienvenue à Antananarivo !')
    .openPopup();
    
  // Correction pour forcer Leaflet à recalculer la taille de la carte 
  // car Ionic peut retarder l'affichage complet du contenu
  setTimeout(() => {
    map.invalidateSize();
  }, 400);
});
</script>

<style scoped>
/* Très important : la carte doit avoir une hauteur définie */
#map {
  width: 100%;
  height: 100%;
  z-index: 0;
}
</style>