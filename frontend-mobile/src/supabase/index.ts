import { createClient } from '@supabase/supabase-js';

// ✅ Avec Vite, on utilise import.meta.env
// Note : Les variables doivent commencer par VITE_ dans votre fichier .env
// const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
// const supabaseKey = import.meta.env.VITE_SUPABASE_ANON_KEY;
const supabaseUrl = 'https://iyjgmdtbpsaglrvdvlpm.supabase.co';
const supabaseKey = 'sb_publishable_y6XIeHJCniYIO8sfMXzAww_7kkTL3-b';

if (!supabaseUrl || !supabaseKey) {
    console.error('Variables VITE_SUPABASE_URL et VITE_SUPABASE_ANON_KEY manquantes dans le .env');
}

export const supabase = createClient(supabaseUrl, supabaseKey);