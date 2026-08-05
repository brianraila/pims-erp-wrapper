package ke.co.safaricom.pims.wrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "ms")
public record MsAesProperties(Map<String, String> aes) {

    public MsAesProperties {
        if (aes == null) {
            aes = Map.of();
        }
    }

    public String currentKey() {
        return aes.get("current-key");
    }

    public String previousKey() {
        return aes.get("previous-key");
    }

    public String algorithm() {
        return aes.getOrDefault("algorithm", "AES/GCM/NoPadding");
    }
}
