<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Connexion</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="login-container">
        <div class="ion-text-center ion-margin-bottom">
          <ion-icon :icon="personCircleOutline" style="font-size: 80px;"></ion-icon>
        </div>

        <ion-item fill="outline" mode="md" class="ion-margin-bottom">
          <ion-input v-model="username" label="Nom d'utilisateur" label-placement="floating"
            placeholder="Entrez votre nom" >
          </ion-input>
        </ion-item>

        <ion-item fill="outline" mode="md" class="ion-margin-bottom">
          <ion-input v-model="password" type="password" label="Mot de passe" label-placement="floating"
            placeholder="********">
          </ion-input>
        </ion-item>

        <ion-button expand="block" @click="handlelogin" class="ion-margin-top">
          Se connecter
        </ion-button>

        <div class="ion-text-center ion-margin-top">
          <p>Pas encore de compte ?</p>
          <ion-button fill="clear" @click="inscription" size="small">S'inscrire</ion-button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonItem, IonLabel, IonInput, IonButton, IonIcon, alertController
} from '@ionic/vue';
import { personCircleOutline } from 'ionicons/icons';
import useAuth from '@/composables/useAuth';

// Data
const username = ref('testuser123@example.com');
const password = ref('123456');
const router = useRouter();
const { login, signin, error, success } = useAuth();

// Votre fonction de vérification factice
const maFonctionDeVerification = (user: string, pass: string): boolean => {
  // Remplacez par votre vraie logique (ex: appel API)
  return true;
  //   return user === 'admin' && pass === '1234';
};

const inscription = () => {
  router.push('/inscription');
}
const handlelogin = async () => {

  const loginResult = await login(username.value, password.value);
  if (success.value) {
    router.push('/home');
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

}

</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto 0;
}
</style>