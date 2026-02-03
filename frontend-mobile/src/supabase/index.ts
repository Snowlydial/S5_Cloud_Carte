import { createClient } from '@supabase/supabase-js';
import dotenv from 'dotenv';

dotenv.config();

const supabaseUrl = process.env.SUPABASE_URL!;
const supabaseKey = process.env.SUPABASE_ANON_KEY!;

if (!supabaseUrl || !supabaseKey) {
    throw new Error('Variables SUPABASE_URL et SUPABASE_ANON_KEY requises dans .env');
}

export const supabase = createClient(supabaseUrl, supabaseKey);