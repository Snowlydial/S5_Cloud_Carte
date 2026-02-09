import { supabase } from '../supabase';

export class SupabaseService {
  /**
   * Upload des photos Capacitor vers Supabase (Compatible Web/Android/iOS)
   */
  static async uploadCapacitorPhotos(photos: any[], folder: string = 'signalements'): Promise<string[]> {
    const uploadPromises = photos.map(async (photo) => {
      try {
        // 1. Récupérer les données de l'image via le webPath
        const response = await fetch(photo.webPath);
        const blob = await response.blob();

        // 2. Générer un nom unique
        const fileName = `photo_${Date.now()}_${Math.random().toString(36).substring(7)}.${photo.format}`;
        const filePath = `${folder}/${fileName}`;

        // 3. Upload vers le Storage
        const { data, error } = await supabase.storage
          .from('signalements')
          .upload(filePath, blob, {
            contentType: `image/${photo.format}`,
            upsert: false
          });

        if (error) throw error;

        // 4. URL publique
        const { data: urlData } = supabase.storage
          .from('signalements')
          .getPublicUrl(filePath);

        return urlData.publicUrl;
      } catch (err) {
        console.error("Erreur upload image:", err);
        throw err;
      }
    });

    return Promise.all(uploadPromises);
  }

  // Note: Supprimez ou commentez uploadImage qui utilise 'fs' 
  // car il ne fonctionnera JAMAIS sur un téléphone ou navigateur.
}