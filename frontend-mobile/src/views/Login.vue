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
            placeholder="Entrez votre nom" :disabled="isLoading">
          </ion-input>
        </ion-item>

        <ion-item fill="outline" mode="md" class="ion-margin-bottom">
          <ion-input v-model="password" type="password" label="Mot de passe" label-placement="floating"
            placeholder="********" :disabled="isLoading">
          </ion-input>
        </ion-item>

        <ion-button expand="block" @click="handlelogin" class="ion-margin-top" :disabled="isLoading">
          <ion-spinner v-if="isLoading" name="crescent" slot="start"></ion-spinner>
          {{ isLoading ? 'Connexion en cours...' : 'Se connecter' }}
        </ion-button>

        <!-- <div class="ion-text-center ion-margin-top">
          <p>Pas encore de compte ?</p>
          <ion-button fill="clear" @click="inscription" size="small" :disabled="isLoading">S'inscrire</ion-button>
        </div> -->
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
const username = ref('a@gmail.com');
const password = ref('aaaaaa');
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
.login-container {
  max-width: 400px;
  margin: 50px auto 0;
}
</style>