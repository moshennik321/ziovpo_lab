package org.example.server.signature;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SigningService {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private final SignatureKeyStoreService keyStoreService;
    private final JsonCanonicalizer jsonCanonicalizer;

    public String sign(Object payload) {
        try {
            byte[] canonicalBytes = jsonCanonicalizer.canonicalizeToUtf8Bytes(payload);
            byte[] signedBytes = sign(canonicalBytes);
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            throw new SignatureException("Failed to sign payload", e);
        }
    }

    public byte[] sign(byte[] payloadBytes) {
        try {
            PrivateKey privateKey = keyStoreService.getPrivateKey();

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(payloadBytes);

            return signature.sign();
        } catch (Exception e) {
            throw new SignatureException("Failed to sign binary payload", e);
        }
    }

    public boolean verify(Object payload, String base64Signature) {
        try {
            byte[] canonicalBytes = jsonCanonicalizer.canonicalizeToUtf8Bytes(payload);
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(keyStoreService.getPublicKey());
            signature.update(canonicalBytes);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            throw new SignatureException("Failed to verify signature", e);
        }
    }
}
