package ke.co.safaricom.pims.wrapper.util;

import ke.co.safaricom.pims.wrapper.config.MsAesProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-GCM decrypt compatible with ms-pims-onboarding-service MsAES.
 */
@Component
public class AesDecryptor {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final MsAesProperties properties;

    public AesDecryptor(MsAesProperties properties) {
        this.properties = properties;
    }

    public String decrypt(String cipherBase64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherBase64);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);

            byte[] cipherBytes = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, cipherBytes, 0, cipherBytes.length);

            try {
                return tryDecrypt(cipherBytes, iv, decode(properties.currentKey()));
            } catch (Exception ignored) {
                // fall through to previous key
            }

            String previous = properties.previousKey();
            if (previous != null && !previous.isBlank()) {
                return tryDecrypt(cipherBytes, iv, decode(previous));
            }

            throw new IllegalStateException("Unable to decrypt with current or previous AES keys");
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private SecretKeySpec decode(String base64) {
        if (base64 == null) {
            throw new IllegalArgumentException("AES key is not configured");
        }
        byte[] key = Base64.getDecoder().decode(base64);
        if (key.length != 32) {
            throw new IllegalArgumentException("AES key must be exactly 32 bytes");
        }
        return new SecretKeySpec(key, "AES");
    }

    private String tryDecrypt(byte[] cipherBytes, byte[] iv, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance(properties.algorithm());
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
        return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
    }
}
