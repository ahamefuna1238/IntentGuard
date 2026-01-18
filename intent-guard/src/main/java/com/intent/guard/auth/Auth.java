package com.intent.guard.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intent.guard.core.token.TokenManager;

/**
 * Internal callback interface for monitoring the lifecycle of security tokens.
 * <p>
 * This interface is used by the security infrastructure to notify observers when
 * tokens are created or when their validity is determined. It bridges the gap
 * between the raw token generation logic and the high-level security management
 * layers of the library.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public interface Auth {

    /**
     * Triggered immediately after a new secure session token is created.
     * <p>
     * Use this callback to perform final configurations on the token or to
     * log the start of a secure session.
     * </p>
     *
     * @param tokenManager The instance managing the newly generated session token.
     */
    void onAccessTokenGenerated(@NonNull TokenManager tokenManager);

    /**
     * Triggered after a token validation attempt has concluded.
     * <p>
     * This method signals whether the incoming response from a target application
     * is trustworthy. If the token is valid, {@code authException} will be null.
     * Otherwise, the exception will contain the specific failure code (e.g., expired or invalid).
     * </p>
     *
     * @param tokenManager   The instance representing the token undergoing validation.
     * @param authException  A {@link AuthException} detailing the failure reason,
     * or {@code null} if validation was successful.
     */
    void onAccessTokenValidated(@NonNull TokenManager tokenManager, @Nullable AuthException authException);
}