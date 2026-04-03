package org.example.server.signature;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

@Service
@RequiredArgsConstructor
public class SignatureKeyStoreService {

    private final SignatureProperties properties;

    private volatile PrivateKey privateKey;
    private volatile PublicKey publicKey;
    private volatile X509Certificate certificate;

    @PostConstruct
    public void init() {
        loadKeys();
    }

    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            synchronized (this) {
                if (privateKey == null) {
                    loadKeys();
                }
            }
        }
        return privateKey;
    }

    public PublicKey getPublicKey() {
        if (publicKey == null) {
            synchronized (this) {
                if (publicKey == null) {
                    loadKeys();
                }
            }
        }
        return publicKey;
    }

    public X509Certificate getCertificate() {
        if (certificate == null) {
            synchronized (this) {
                if (certificate == null) {
                    loadKeys();
                }
            }
        }
        return certificate;
    }

    private void loadKeys() {
        try (InputStream is = openKeyStoreStream(properties.getKeyStorePath())) {
            if (properties.getKeyStorePath() == null || properties.getKeyStorePath().isBlank()) {
                throw new SignatureException("signature.key-store-path is not configured");
            }
            if (properties.getKeyStorePassword() == null) {
                throw new SignatureException("signature.key-store-password is not configured");
            }
            if (properties.getKeyAlias() == null || properties.getKeyAlias().isBlank()) {
                throw new SignatureException("signature.key-alias is not configured");
            }

            KeyStore keyStore = KeyStore.getInstance(properties.getKeyStoreType());
            keyStore.load(is, properties.getKeyStorePassword().toCharArray());

            String keyPassword = (properties.getKeyPassword() == null || properties.getKeyPassword().isBlank())
                    ? properties.getKeyStorePassword()
                    : properties.getKeyPassword();

            Key key = keyStore.getKey(properties.getKeyAlias(), keyPassword.toCharArray());
            if (key == null) {
                throw new SignatureException("Private key not found for alias: " + properties.getKeyAlias());
            }
            if (!(key instanceof PrivateKey pk)) {
                throw new SignatureException("Key is not a private key for alias: " + properties.getKeyAlias());
            }

            var cert = keyStore.getCertificate(properties.getKeyAlias());
            if (cert == null) {
                throw new SignatureException("Certificate not found for alias: " + properties.getKeyAlias());
            }
            if (!(cert instanceof X509Certificate x509)) {
                throw new SignatureException("Certificate is not X509 for alias: " + properties.getKeyAlias());
            }

            this.privateKey = pk;
            this.publicKey = x509.getPublicKey();
            this.certificate = x509;

        } catch (Exception e) {
            throw new SignatureException("Failed to load keystore and signing keys", e);
        }
    }

    private InputStream openKeyStoreStream(String path) throws Exception {
        if (path == null || path.isBlank()) {
            throw new SignatureException("signature.key-store-path is empty");
        }

        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            return new ClassPathResource(resourcePath).getInputStream();
        }

        if (path.startsWith("file:")) {
            String filePath = path.substring("file:".length());
            return new FileInputStream(filePath);
        }

        return new FileInputStream(path);
    }
}