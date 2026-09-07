package com.hardware.shop.util;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthSessionManager {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private static final long SESSION_DURATION_SECONDS = 8 * 60 * 60;

    private AuthSessionManager() {
    }

    public static String createSession(int userId, String username) {

        String token = UUID.randomUUID().toString();

        Instant expiresAt = Instant.now().plusSeconds(SESSION_DURATION_SECONDS);

        SESSIONS.put(token, new Session(userId, username, expiresAt));

        return token;
    }

    public static boolean isValidToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        Session session = SESSIONS.get(token);

        if (session == null) {
            return false;
        }

        if (Instant.now().isAfter(session.expiresAt())) {

            SESSIONS.remove(token);
            return false;
        }

        return true;
    }

    public static void removeSession(String token) {

        if (token != null) {
            SESSIONS.remove(token);
        }
    }

    public static Session getSession(String token) {

        if (!isValidToken(token)) {
            return null;
        }

        return SESSIONS.get(token);
    }

    public record Session(int userId, String username, Instant expiresAt) {
    }
}