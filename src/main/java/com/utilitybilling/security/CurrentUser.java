package com.utilitybilling.security;

import com.utilitybilling.user.entity.User;

/** Authenticated user projection used by services. */
public record CurrentUser(Long id, String email) {
    public static CurrentUser from(User user) {
        return new CurrentUser(user.getId(), user.getEmail());
    }
}
