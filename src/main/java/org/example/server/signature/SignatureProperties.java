package org.example.server.signature;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {

    /**
     * classpath:signing.jks
     * file:C:/keys/signing.jks
     * или обычный путь
     */
    private String keyStorePath;

    /**
     * JKS или PKCS12
     */
    private String keyStoreType = "JKS";

    private String keyStorePassword;

    private String keyAlias;

    /**
     * Если не задан, будем использовать keyStorePassword
     */
    private String keyPassword;
}