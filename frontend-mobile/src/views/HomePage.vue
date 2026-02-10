<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title>Accueil</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <div class="neo-container">
        <div class="neo-welcome-card">
          <h1 class="neo-title">Cloud Carte</h1>
          <p class="neo-subtitle">Prêt à cartographier ?</p>
          <p class="neo-description">
            Commencez avec les 
            <a target="_blank" rel="noopener noreferrer" href="https://ionicframework.com/docs/components">
              composants Ionic
            </a>
          </p>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/vue';
</script>

<style scoped>
.neo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
  background: var(--neo-background, #F5F6FA);
}

.neo-welcome-card {
  background: var(--neo-background, #F5F6FA);
  border: 4px solid var(--neo-dark, #2C3A47);
  box-shadow: 8px 8px 0 var(--neo-dark, #2C3A47);
  padding: 48px 32px;
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.neo-title {
  font-size: 32px;
  font-weight: 900;
  color: var(--neo-dark, #2C3A47);
  margin: 0 0 12px 0;
  text-transform: uppercase;
  letter-spacing: -1px;
  font-family: var(--neo-font, 'Space Mono', monospace);
}

.neo-subtitle {
  font-size: 16px;
  font-weight: 700;
  color: var(--neo-dark, #2C3A47);
  margin: 0 0 24px 0;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-family: var(--neo-font, 'Space Mono', monospace);
}

.neo-description {
  font-size: 13px;
  color: var(--neo-dark, #2C3A47);
  margin: 0;
  font-family: var(--neo-font, 'Space Mono', monospace);
}

.neo-description a {
  color: var(--neo-primary, #FFC312);
  background: var(--neo-dark, #2C3A47);
  text-decoration: none;
  padding: 2px 8px;
  font-weight: 700;
  transition: all 0.1s ease;
}

.neo-description a:hover {
  background: var(--neo-primary, #FFC312);
  color: var(--neo-dark, #2C3A47);
}
</style>
