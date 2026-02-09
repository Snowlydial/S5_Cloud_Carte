package com.example.carte.util;

import java.io.*;
import java.util.Base64;

public class ImageUtil {

    // Convertir une image en Base64
    public static String encodeImageToBase64(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();

        return Base64.getEncoder().encodeToString(bytes);
    }
}

