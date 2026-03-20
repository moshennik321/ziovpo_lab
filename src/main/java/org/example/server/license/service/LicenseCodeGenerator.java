package org.example.server.license.service;

import lombok.RequiredArgsConstructor;
import org.example.server.license.repository.LicenseRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class LicenseCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PART_LENGTH = 4;
    private static final int PARTS = 4;

    private final SecureRandom secureRandom = new SecureRandom();
    private final LicenseRepository licenseRepository;

    public String generateCode() {
        String code;
        do {
            code = randomCode();
        } while (licenseRepository.existsByCode(code));

        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder();

        for (int part = 0; part < PARTS; part++) {
            if (part > 0) {
                sb.append("-");
            }
            for (int i = 0; i < PART_LENGTH; i++) {
                sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
            }
        }

        return sb.toString();
    }
}