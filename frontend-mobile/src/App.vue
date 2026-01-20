<template>
  <ion-app>
    <ion-router-outlet />
  </ion-app>
</template>

<script setup lang="ts">
import { alertController, IonApp, IonRouterOutlet } from '@ionic/vue';
import { onAuthStateChanged, User } from 'firebase/auth';
import { onMounted } from 'vue';
import { ref } from 'vue';
import router from './router';
import { auth } from './firebase';
import { Util } from './utils/util';

const user = ref<User | null>(null);

onMounted(() => {
  onAuthStateChanged(auth, async (u) => {
    if (u && await Util.checkSession()) {
      user.value = u;
      console.log("Utilisateur connecté :", u);
    } else {
      console.log("Session expirée ou utilisateur non connecté.");
      const expired = localStorage.getItem("expired");
      if (expired) {

        const alert = await alertController.create({
          header: 'Échec de connexion',
          message: 'Votre session a expiré. Veuillez vous reconnecter.',
          buttons: ['OK'],
        });
        await alert.present();
      }
      localStorage.removeItem("expired");
      user.value = null;
      router.push("/login");

    }
  });
});
</script>
