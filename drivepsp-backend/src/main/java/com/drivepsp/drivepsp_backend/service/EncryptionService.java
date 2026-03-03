package com.drivepsp.drivepsp_backend.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cifra y descifra los archivos de los usuarios con AES-256-GCM antes de
 * guardarlos en disco. GCM proporciona confidencialidad e integridad a la vez
 * gracias a su etiqueta de autenticacion.
 *
 * Cada archivo lleva un IV aleatorio de 12 bytes que se antepone al contenido
 * cifrado, de forma que el formato en disco es: IV (12 bytes) + datos cifrados + tag GCM.
 *
 * La clave AES de 256 bits se deriva del secreto configurado en application.yml
 * aplicandole SHA-256, lo que garantiza siempre exactamente 32 bytes.
 */
@Service
public class EncryptionService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec claveAes;

    /**
     * Inicializa la clave AES derivando el secreto de configuracion con SHA-256
     * para obtener exactamente 32 bytes independientemente de su longitud.
     *
     * @param secreto cadena secreta configurada en application.yml
     */
    public EncryptionService(@Value("${drivepsp.encryption.secret}") String secreto) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(secreto.getBytes());
            this.claveAes = new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar la clave de cifrado", e);
        }
    }

    /**
     * Cifra los datos con AES-256-GCM generando un IV aleatorio por cada llamada.
     * El resultado tiene el formato: IV (12 bytes) seguido de los datos cifrados.
     *
     * @param datos bytes del archivo original
     * @return bytes cifrados con el IV al inicio
     */
    public byte[] cifrar(byte[] datos) {
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, claveAes, spec);

            byte[] datosCifrados = cipher.doFinal(datos);

            byte[] resultado = new byte[IV_BYTES + datosCifrados.length];
            System.arraycopy(iv, 0, resultado, 0, IV_BYTES);
            System.arraycopy(datosCifrados, 0, resultado, IV_BYTES, datosCifrados.length);

            return resultado;
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar los datos", e);
        }
    }

    /**
     * Descifra datos previamente cifrados con cifrar(). Extrae el IV de los
     * primeros 12 bytes y descifra el resto con AES-256-GCM.
     *
     * @param datosCifrados bytes con formato IV + datos cifrados
     * @return bytes del archivo original
     */
    public byte[] descifrar(byte[] datosCifrados) {
        try {
            byte[] iv = new byte[IV_BYTES];
            System.arraycopy(datosCifrados, 0, iv, 0, IV_BYTES);

            int longitudCifrado = datosCifrados.length - IV_BYTES;
            byte[] contenidoCifrado = new byte[longitudCifrado];
            System.arraycopy(datosCifrados, IV_BYTES, contenidoCifrado, 0, longitudCifrado);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, claveAes, spec);

            return cipher.doFinal(contenidoCifrado);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar los datos", e);
        }
    }
}
