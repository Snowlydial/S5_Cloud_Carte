import { supabase } from '../supabase';
import fs from 'fs';
import path from 'path';
import { dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export class SupabaseService {
    /**
     * Upload une image vers Supabase Storage
     * @param filePath Chemin du fichier à uploader
     * @param folder Dossier de destination (optionnel)
     * @returns URL publique de l'image uploadée
     */
    static async uploadImage(filePath?: string, folder: string = 'signalements') {
        try {
            // Chemin par défaut si non fourni
            const defaultPath = path.join(__dirname, "../../public/assets/images", "favicon.png");
            const targetPath = filePath || defaultPath;

            if (!fs.existsSync(targetPath)) {
                throw new Error("Fichier non trouvé : " + targetPath);
            }

            console.log(`📤 Upload vers Supabase (bucket: ${folder})...`);

            // Lire le fichier
            const fileBuffer = fs.readFileSync(targetPath);
            const fileName = `${path.basename(targetPath, path.extname(targetPath))}_${Date.now()}${path.extname(targetPath)}`;
            const uploadPath = folder ? `${folder}/${fileName}` : fileName;

            // Upload vers Supabase Storage
            const { data, error } = await supabase.storage
                .from('signalements') // Nom du bucket
                .upload(uploadPath, fileBuffer, {
                    contentType: this.getMimeType(targetPath),
                    upsert: false
                });

            if (error) {
                throw new Error(`Erreur Supabase: ${error.message}`);
            }

            // Récupérer l'URL publique
            const { data: urlData } = supabase.storage
                .from('signalements')
                .getPublicUrl(uploadPath);

            console.log("✅ Upload réussi !");
            console.log("🔗 URL:", urlData.publicUrl);
            
            return urlData.publicUrl;
        } catch (err) {
            console.error("❌ Erreur upload:", err);
            throw err;
        }
    }

    /**
     * Upload plusieurs images en une fois
     * @param filePaths Array de chemins de fichiers
     * @param folder Dossier de destination
     * @returns Array d'URLs publiques
     */
    static async uploadMultipleImages(filePaths: string[], folder: string = 'signalements'): Promise<string[]> {
        const uploadPromises = filePaths.map(filePath => this.uploadImage(filePath, folder));
        return Promise.all(uploadPromises);
    }

    /**
     * Supprimer une image de Supabase Storage
     * @param fileName Nom du fichier à supprimer
     * @param folder Dossier où se trouve le fichier
     */
    static async deleteImage(fileName: string, folder: string = 'signalements'): Promise<void> {
        try {
            const filePath = folder ? `${folder}/${fileName}` : fileName;

            const { error } = await supabase.storage
                .from('signalements')
                .remove([filePath]);

            if (error) {
                throw new Error(`Erreur suppression: ${error.message}`);
            }

            console.log("🗑️  Image supprimée:", fileName);
        } catch (err) {
            console.error("❌ Erreur suppression:", err);
            throw err;
        }
    }

    /**
     * Lister les fichiers dans un dossier
     * @param folder Dossier à lister
     */
    static async listFiles(folder: string = 'signalements') {
        try {
            const { data, error } = await supabase.storage
                .from('signalements')
                .list(folder);

            if (error) {
                throw new Error(`Erreur listage: ${error.message}`);
            }

            return data;
        } catch (err) {
            console.error("❌ Erreur listage:", err);
            throw err;
        }
    }

    /**
     * Déterminer le type MIME d'un fichier
     * @param filePath Chemin du fichier
     */
    private static getMimeType(filePath: string): string {
        const ext = path.extname(filePath).toLowerCase();
        const mimeTypes: { [key: string]: string } = {
            '.png': 'image/png',
            '.jpg': 'image/jpeg',
            '.jpeg': 'image/jpeg',
            '.gif': 'image/gif',
            '.webp': 'image/webp',
            '.svg': 'image/svg+xml',
            '.pdf': 'application/pdf'
        };
        return mimeTypes[ext] || 'application/octet-stream';
    }
}