import { useState, useEffect } from 'react';

export const useOfflineMap = () => {
    const [isOnline, setIsOnline] = useState(true);
    
    const TILE_SERVER_URL = 'http://localhost:8081';
    
    useEffect(() => {
        const checkOnline = async () => {
            try {
                await fetch('https://tile.openstreetmap.org/0/0/0.png', {
                    method: 'HEAD',
                    mode: 'no-cors',
                    cache: 'no-store'
                });
                setIsOnline(true);
            } catch {
                setIsOnline(false);
            }
        };
        
        checkOnline();
        const interval = setInterval(checkOnline, 60000);
        return () => clearInterval(interval);
    }, []);

    const tileUrl = isOnline 
        ? "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        : `${TILE_SERVER_URL}/styles/osm-bright/{z}/{x}/{y}.png`;

    return {
        isOnline,
        tileUrl,
        attribution: '© OpenStreetMap contributors'
    };
};