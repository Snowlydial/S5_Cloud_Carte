import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Capacitor } from '@capacitor/core';

export const PhotoService = {
  async selectFromGallery() {
    try {
      // 1. Demande de permissions (géré automatiquement par Capacitor sur le Web)
      if (Capacitor.getPlatform() !== 'web') {
        const permissions = await Camera.checkPermissions();
        if (permissions.photos !== 'granted') {
          await Camera.requestPermissions({ permissions: ['photos'] });
        }
      }

      // 2. Sélection de l'image
      const image = await Camera.getPhoto({
        quality: 90,
        allowEditing: false,
        resultType: CameraResultType.Uri, // On utilise l'Uri pour la performance
        source: CameraSource.Photos
      });

      // 3. Transformation pour la compatibilité (si besoin de l'envoyer plus tard)
      // webPath fonctionne sur Android, iOS et Web pour l'aperçu <img src="...">
      return {
        format: image.format,
        webPath: image.webPath,
        // On peut stocker le path natif pour les traitements lourds sur mobile
        path: image.path 
      };

    } catch (error) {
      console.error("Erreur lors de la sélection de la photo", error);
      return null;
    }
  }
};