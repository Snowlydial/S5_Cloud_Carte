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
            <ion-input 
                v-model="username" 
                label="Nom d'utilisateur" 
                label-placement="floating" 
                placeholder="Entrez votre nom">
            </ion-input>
            </ion-item>

            <ion-item fill="outline" mode="md" class="ion-margin-bottom">
            <ion-input 
                v-model="password" 
                type="password" 
                label="Mot de passe" 
                label-placement="floating" 
                placeholder="********">
            </ion-input>
        </ion-item>

        <ion-button expand="block" @click="handleLogin" class="ion-margin-top">
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

// Data
const username = ref('');
const password = ref('');
const router = useRouter();

// Votre fonction de vérification factice
const maFonctionDeVerification = (user: string, pass: string): boolean => {
  // Remplacez par votre vraie logique (ex: appel API)
  return true;
//   return user === 'admin' && pass === '1234';
};

const inscription = () => {
  router.push('/inscription');
}
// Logique du bouton
const handleLogin = async () => {
  const isSuccess = maFonctionDeVerification(username.value, password.value);

  if (isSuccess) {
    // Redirection vers la page d'accueil (nommée 'Home' dans votre router)
    router.push('/home');
  } else {
    // Message d'erreur si échec
    const alert = await alertController.create({
      header: 'Échec de connexion',
      message: 'Identifiants incorrects. Veuillez réessayer.',
      buttons: ['OK'],
    });
    await alert.present();
    
    // Reset des champs (optionnel)
    password.value = '';
  }
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto 0;
}
</style>