package com.intent.guard.core.token;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.intent.guard.core.Session;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal engine for managing short-lived, single-use authentication tokens.
 * <p>
 * This class provides a high-concurrency storage system for session keys. It is
 * responsible for:
 * <ul>
 * <li><b>Generation:</b> Creating unique keys via {@link Session} and assigning expiration timestamps.</li>
 * <li><b>Validation:</b> Checking if a token exists and is within its valid timeframe.</li>
 * <li><b>Auto-Cleanup:</b> Periodically purging expired entries to prevent memory leaks in long-running processes.</li>
 * <li><b>Consumption:</b> Enforcing "Single-Use" policies by removing tokens immediately after successful validation.</li>
 * </ul>
 *
 * <p>Thread-safety is guaranteed using a {@link ConcurrentHashMap} for the token registry.</p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class TokenManager {

    /** Interval (in ms) to run token cleanup to avoid frequent scans. Currently set to 1 minute. */
    private static final long CLEANUP_INTERVAL_MS = 60000;

    /** Last system time (ms) when the cleanup thread ran. */
    private long lastCleanupTime = 0;

    /** Stores generated tokens mapped to their specific {@link LocalDateTime} expiration times. */
    private final ConcurrentHashMap<String, LocalDateTime> tokenExpiryMap
            = new ConcurrentHashMap<>();

    /** The duration in minutes for which a token remains valid after generation. */
    private final int validityMinutes;

    /** The session generator responsible for the cryptographic complexity of the token strings. */
    private final Session session;

    /** Caches the most recently generated token for quick access by the generator. */
    private String lastGeneratedKey;

    /**
     * Constructs a new TokenManager with a specific session type and validity period.
     *
     * @param session     The {@link Session} instance used to generate the raw token strings.
     * @param validPeriod Lifetime of a token in minutes (clamped to a minimum of 1).
     */
    public TokenManager(Session session, int validPeriod) {
        this.session = session;
        this.validityMinutes = Math.max(validPeriod, 1);
    }

    /**
     * Updates the underlying session generator.
     *
     * @param session The new session configuration to adopt.
     */
    public void delegateToSession(@NonNull Session session) {
        this.session.replace(session);
    }

    /**
     * Generates a unique token string and records its expiration time.
     * <p>The expiration is calculated as {@code LocalDateTime.now() + validityMinutes}.</p>
     */
    public void generateToken() {
        lastGeneratedKey = session.generateSessionCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(validityMinutes);
        tokenExpiryMap.put(lastGeneratedKey, expiry);
    }

    /**
     * Validates a token string and triggers a background cleanup if the interval has passed.
     *
     * @param token The token string to verify.
     * @return {@code true} if the token exists and has not yet expired.
     */
    public boolean isValidToken(@NonNull String token) {
        cleanupExpiredTokensIfNeeded();
        return tokenIsValid(token);
    }

    /**
     * Permanently removes a token from the registry.
     * <p>This should be called immediately after a successful authentication to prevent replay attacks.</p>
     *
     * @param token The token to invalidate.
     */
    public void consumeToken(@NonNull String token) {
        if (isValidToken(token)) {
            tokenExpiryMap.remove(token);
        }
    }

    /**
     * Checks if a token exists in the registry without strictly validating expiration.
     *
     * @param key The token string to look up.
     * @return {@code true} if the key is present in the map.
     */
    public boolean tokenExist(@NonNull String key){
        LocalDateTime tokenExpiryDate = tokenExpiryMap.get(key);
        cleanupExpiredTokensIfNeeded();
        return tokenExpiryDate != null;
    }

    /**
     * Internal logic to check if the token's timestamp is after the current system time.
     */
    private boolean tokenIsValid(@NonNull String token){
        LocalDateTime tokenExpiryDate = tokenExpiryMap.get(token);

        if (tokenExpiryDate == null) {
            return false;
        }
        return !tokenExpiryDate.isBefore(LocalDateTime.now());
    }

    /**
     * Checks the elapsed time since the last cleanup and runs a purge if necessary.
     */
    private void cleanupExpiredTokensIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            cleanupExpiredTokens();
            lastCleanupTime = now;
        }
    }

    /**
     * Iterates through the map and removes all entries where the expiration date is in the past.
     */
    private void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenExpiryMap.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    /**
     * Retrieves the most recently generated key.
     *
     * @return The cached token string.
     * @throws IllegalStateException if called before {@link #generateToken()}.
     */
    @NonNull
    public String getLastGeneratedKey() {
        if (lastGeneratedKey == null) {
            throw new IllegalStateException("Token not generated yet. Call generateToken() first.");
        }
        return lastGeneratedKey;
    }

    /**
     * Returns the length of tokens generated by the current session.
     */
    public int getTokenLength() {
        return session.getCodeLength();
    }

    /**
     * Checks if the current session has successfully generated at least one code.
     */
    public boolean isTokenGenerated() {
        return session.isGenerated();
    }
}