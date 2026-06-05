package com.utilitybilling.common;

import com.utilitybilling.exception.BusinessException;

/** Small shared validation helpers for cross-field rules. */
public final class ValidationSupport {
    private ValidationSupport() {}

    public static void passwordDoesNotContainIdentity(String password, String email, String name, String phone) {
        String p = password.toLowerCase();
        if (p.contains(email.toLowerCase()) || p.contains(phone.toLowerCase())) {
            throw new BusinessException("Password must not contain email or phone number");
        }
        for (String part : name.toLowerCase().split("\\s+")) {
            if (!part.isBlank() && p.contains(part)) {
                throw new BusinessException("Password must not contain the user's name");
            }
        }
    }
}
