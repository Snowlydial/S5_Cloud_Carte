<template>
  <ion-page>

    <ion-content class="ion-padding">
      <div class="login-container">
        <div class="login-card">
          <div class="login-icon-wrapper">
            <ion-icon :icon="personCircleOutline" class="login-icon"></ion-icon>
          </div>

          <h1 class="login-title">Connexion</h1>
          <p class="login-subtitle">Accédez à votre espace</p>

          <div class="form-group">
            <label class="form-label">Nom d'utilisateur</label>
            <ion-item fill="outline" class="neo-input">
              <ion-input v-model="username" placeholder="Entrez votre nom" :disabled="isLoading">
              </ion-input>
            </ion-item>
          </div>

          <div class="form-group">
            <label class="form-label">Mot de passe</label>
            <ion-item fill="outline" class="neo-input">
              <ion-input v-model="password" type="password" placeholder="********" :disabled="isLoading">
              </ion-input>
            </ion-item>
          </div>

          <ion-button expand="block" @click="handlelogin" class="login-btn" :disabled="isLoading">
            <ion-spinner v-if="isLoading" name="crescent" slot="start"></ion-spinner>
            {{ isLoading ? 'Connexion...' : 'Se connecter' }}
          </ion-button>
        </div>
      </div>

      <ion-loading
        :is-open="isLoading"
        message="Veuillez patienter..."
        spinner="circles">
      </ion-loading>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonItem, IonInput, IonButton, IonIcon, IonSpinner, IonLoading, alertController
} from '@ionic/vue';
import { personCircleOutline } from 'ionicons/icons';
import useAuth from '@/composables/useAuth';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '@/firebase';



// Data
const username = ref('toto@gmail.com');
const password = ref('111111');
const isLoading = ref(false); // État de chargement
const router = useRouter();
const { login, error, success } = useAuth();

const inscription = () => {
  router.push('/inscription');
}

const handlelogin = async () => {
  // 1. Activer le chargement
  isLoading.value = true;

  try {
    await login(username.value, password.value);

    if (success.value) {
      router.push('/home');
    } else {
      const alert = await alertController.create({
        header: 'Échec de connexion',
        message: error.value || 'Nom d\'utilisateur ou mot de passe incorrect.',
        buttons: ['OK'],
        cssClass: 'neo-alert'
      });
      await alert.present();
    }
  } catch (err) {
    console.error(err);
  } finally {
    // 2. Désactiver le chargement (dans tous les cas : succès ou erreur)
    isLoading.value = false;
  }
}

// onMounted(async () => {
//   await signInWithEmailAndPassword(auth, "a@gmail.com", "aaaaaa");
//   console.log("Auto-login successful"+ auth.currentUser?.email);
// });
</script>


<style scoped>
/* Neobrutalism Login Styles */
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 100px);
  padding: 20px;
}

.login-card {
  background: #F5F6FA;
  padding: 40px 32px;
  border: 4px solid #2C3A47;
  box-shadow: 8px 8px 0 #2C3A47;
  width: 100%;
  max-width: 400px;
}

.login-icon-wrapper {
  text-align: center;
  margin-bottom: 24px;
}

.login-icon {
  font-size: 72px;
  color: #2C3A47;
}

.login-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 900;
  color: #2C3A47;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: -1px;
  font-family: 'Space Mono', monospace;
}

.login-subtitle {
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

.login-btn {
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