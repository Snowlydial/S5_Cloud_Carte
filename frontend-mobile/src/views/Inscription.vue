<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/login"></ion-back-button>
        </ion-buttons>
        <ion-title>Inscription</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="register-container">
        <div class="ion-text-center ion-margin-bottom">
          <ion-icon :icon="personAddOutline" style="font-size: 80px;"></ion-icon>
        </div>

        <ion-item fill="outline" mode="md" class="ion-margin-bottom">
          <ion-input v-model="username" label="Nom d'utilisateur" label-placement="floating"
            placeholder="Choisissez un nom">
          </ion-input>
        </ion-item>

        <ion-item fill="outline" mode="md" class="ion-margin-bottom">
          <ion-input v-model="password" type="password" label="Mot de passe" label-placement="floating"
            placeholder="********">
          </ion-input>
        </ion-item>

        <ion-button expand="block" @click="handleRegister" class="ion-margin-top">
          Créer mon compte
        </ion-button>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonItem,
  IonLabel, IonInput, IonButton, IonIcon, IonButtons, IonBackButton,
  IonDatetime, IonDatetimeButton, IonModal, toastController , alertController
} from '@ionic/vue';
import { personAddOutline } from 'ionicons/icons';
import useAuth from '@/composables/useAuth';

const router = useRouter();
const username = ref('');
const password = ref('');
const birthDate = ref('');

const { signin, error, success } = useAuth();

const handleRegister = async () => {
  const signinResult = await signin(username.value, password.value);
  if (success.value) {
    router.push('/login');
    const toast = await toastController.create({
      message: 'Compte créé avec succès !',
      duration: 2000,
      color: 'success',
      position: 'top'
    });
    await toast.present();
    return;
  }
  else {

    const alert = await alertController.create({
      header: 'Échec de connexion',
      message: error.value || 'Nom d\'utilisateur ou mot de passe incorrect.',
      buttons: ['OK'],
    });
    await alert.present();
  }
};
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 30px auto 0;
}
</style>