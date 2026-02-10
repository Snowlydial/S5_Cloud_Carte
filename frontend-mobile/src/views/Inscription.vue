<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/login"></ion-back-button>
        </ion-buttons>
        <ion-title>Inscription</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="register-container">
        <div class="register-card">
          <div class="register-icon-wrapper">
            <ion-icon :icon="personAddOutline" class="register-icon"></ion-icon>
          </div>

          <h1 class="register-title">Inscription</h1>
          <p class="register-subtitle">Créez votre compte</p>

          <div class="form-group">
            <label class="form-label">Nom d'utilisateur</label>
            <ion-item fill="outline" class="neo-input">
              <ion-input v-model="username" placeholder="Choisissez un nom">
              </ion-input>
            </ion-item>
          </div>

          <div class="form-group">
            <label class="form-label">Mot de passe</label>
            <ion-item fill="outline" class="neo-input">
              <ion-input v-model="password" type="password" placeholder="********">
              </ion-input>
            </ion-item>
          </div>

          <ion-button expand="block" @click="handleRegister" class="register-btn">
            Créer mon compte
          </ion-button>
        </div>
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
      cssClass: 'neo-alert'
    });
    await alert.present();
  }
};
</script>

<style scoped>
/* Neobrutalism Registration Styles */
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 100px);
  padding: 20px;
}

.register-card {
  background: #F5F6FA;
  padding: 40px 32px;
  border: 4px solid #2C3A47;
  box-shadow: 8px 8px 0 #2C3A47;
  width: 100%;
  max-width: 400px;
}

.register-icon-wrapper {
  text-align: center;
  margin-bottom: 24px;
}

.register-icon {
  font-size: 72px;
  color: #2C3A47;
}

.register-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 900;
  color: #2C3A47;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: -1px;
  font-family: 'Space Mono', monospace;
}

.register-subtitle {
  text-align: center;
  color: #2C3A47;
  margin: 0 0 32px 0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
  font-family: 'Space Mono', monospace;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 11px;
  font-weight: 900;
  color: #2C3A47;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
  font-family: 'Space Mono', monospace;
}

.neo-input {
  --background: #F5F6FA;
  --border-width: 3px;
  --border-color: #2C3A47;
  --border-radius: 0;
  --highlight-color-focused: #4ECDC4;
  transition: all 0.1s ease;
}

.neo-input:focus-within {
  --background: #4ECDC4;
  box-shadow: 4px 4px 0 #2C3A47;
  transform: translate(-2px, -2px);
}

.register-btn {
  margin-top: 24px;
  --padding-top: 16px;
  --padding-bottom: 16px;
  --background: #4ECDC4;
  --color: #2C3A47;
  --border-color: #2C3A47;
}

/* Toolbar Styling */
ion-toolbar {
  --background: #2C3A47;
  --color: #F5F6FA;
}

ion-title {
  color: #F5F6FA;
  font-family: 'Space Mono', monospace;
  font-weight: 900;
}

/* Ion-Content Background */
ion-content {
  --background: #F5F6FA;
}

/* Input Text Color */
.neo-input ion-input {
  --color: #2C3A47;
}
</style>