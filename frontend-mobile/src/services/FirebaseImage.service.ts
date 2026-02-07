export class FirebaseImageService {
  static async processAndCompress(photo: any): Promise<string> {
    const response = await fetch(photo.webPath);
    const blob = await response.blob();
    
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = URL.createObjectURL(blob);
      
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        // 1. Définir des dimensions max (ex: 1024px)
        const MAX_WIDTH = 1024;
        let width = img.width;
        let height = img.height;

        if (width > MAX_WIDTH) {
          height *= MAX_WIDTH / width;
          width = MAX_WIDTH;
        }

        canvas.width = width;
        canvas.height = height;

        // 2. Dessiner l'image sur le canvas
        ctx?.drawImage(img, 0, 0, width, height);

        // 3. Convertir en Base64 avec compression JPEG (0.6 = 60% qualité)
        // C'est ici que la magie opère pour réduire le poids !
        const compressedBase64 = canvas.toDataURL('image/jpeg', 0.6);
        
        resolve(compressedBase64);
      };
      
      img.onerror = reject;
    });
  }
  
  static async toBase64(photo: any): Promise<string> {
    const response = await fetch(photo.webPath);
    const blob = await response.blob();
    
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

  /**
   * Compresse et convertit les photos Capacitor en Base64
   */
  static async processPhotosForFirestore(photos: any[]): Promise<string[]> {
    const processPromises = photos.map(async (photo) => {
      try {
        // 1. Récupérer l'image depuis le webPath
        const response = await fetch(photo.webPath);
        const blob = await response.blob();
        
        // 2. Compresser et convertir en Base64
        return await this.compressImage(blob);
      } catch (err) {
        console.error("Erreur traitement image:", err);
        throw err;
      }
    });

    return Promise.all(processPromises);
  }

  private static compressImage(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = URL.createObjectURL(blob);
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const MAX_WIDTH = 800; // Taille max pour rester sous 1Mo par document
        const scale = MAX_WIDTH / img.width;
        
        canvas.width = MAX_WIDTH;
        canvas.height = img.height * scale;

        const ctx = canvas.getContext('2d');
        ctx?.drawImage(img, 0, 0, canvas.width, canvas.height);

        // Conversion en Base64 avec compression JPEG à 70%
        const base64 = canvas.toDataURL('image/jpeg', 0.7);
        resolve(base64);
      };
      img.onerror = reject;
    });
  }
}