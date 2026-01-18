package com.intent.guard.core.access;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intent.guard.auth.Auth;
import com.intent.guard.auth.AuthException;
import com.intent.guard.core.Session;
import com.intent.guard.core.token.TokenManager;
import com.intent.guard.core.trusted.TrustedRequesterManager;


/**
 * Abstract base class responsible for managing secure access tokens and trusted application validation.
 * <p>
 * This manager acts as the central hub for the IntentGuard security protocol. It orchestrates:
 * <ul>
 * <li><b>Token Management:</b> Generation and validation of time-sensitive session keys.</li>
 * <li><b>Trust Enforcement:</b> Whitelisting trusted applications by package name and SHA-256 certificate fingerprints.</li>
 * <li><b>Data Encapsulation:</b> Managing the flow of {@link Intent} and {@link Bundle} data between requester and responder.</li>
 * </ul>
 *
 * <p>To implement a custom security policy, extend this class and implement the abstract lifecycle callbacks:
 * <ul>
 * <li>{@link #onAccessTokenGenerated(AccessManager, String)}</li>
 * <li>{@link #onValidAccessToken(TokenManager, AuthException)}</li>
 * </ul>
 *
 * @since 1.0
 * @author David Onyia
 */
public abstract class AccessManager {

    /** Internal handler for the cryptographic generation and verification of tokens. */
    private final TokenManager tokenManager;

    /** Singleton instance for managing package-level trust verification. */
    private final TrustedRequesterManager trustedRequesterManager
            = TrustedRequesterManager.getInstance();

    private Context applicationContext;

    @Nullable
    private AuthException authException;

    /** The data payload returned to the requesting application after successful authentication. */
    @Nullable
    private Bundle resultBody;

    /** The payload bundle to be sent as part of a response. */
    private Bundle responseBody;

    /** The original intent that initiated the current transaction. */
    private Intent incomingIntent;

    /** Toggle for package/certificate-based caller verification. */
    private boolean enableTrustedApp;

    /** Toggle for session token security. */
    private boolean enableCodeGeneration;

    /** * Bridge between the internal {@link Auth} interface and the
     * protected abstract methods of this manager.
     */
    private final Auth auth = new Auth() {
        @Override
        public void onAccessTokenGenerated(@NonNull TokenManager tokenManager) {
            AccessManager.this.onAccessTokenGenerated(AccessManager.this, tokenManager.getLastGeneratedKey());
        }

        @Override
        public void onAccessTokenValidated(@NonNull TokenManager tokenManager, @Nullable AuthException authException) {
            AccessManager.this.authException = authException;
            onValidAccessToken(tokenManager, authException);
        }
    };

    /**
     * Initializes the manager with a specific token validity duration.
     *
     * @param tokenValidity The duration in minutes for which a token remains valid (minimum 1).
     */
    public AccessManager(int tokenValidity) {
        this(tokenValidity, null);
    }

    /**
     * Initializes the manager with a custom session configuration.
     *
     * @param customSession A custom {@link Session} generator (defaults to a 37-char alphanumeric session if null).
     */
    public AccessManager(@Nullable Session customSession) {
        this(1, customSession);
    }

    /**
     * Master constructor for configuring token lifespan and session complexity.
     *
     * @param tokenValidity Duration in minutes for token validity.
     * @param customSession Custom session generator.
     */
    protected AccessManager(int tokenValidity, @Nullable Session customSession) {
        this.tokenManager = new TokenManager(customSession != null ? customSession : new Session(37),
                Math.max(tokenValidity, 1));
    }

    /**
     * Callback invoked immediately after a new session token is successfully generated.
     *
     * @param accessManager The current instance of the manager.
     * @param sessionKey    The newly generated secure session key.
     */
    protected abstract void onAccessTokenGenerated(@NonNull AccessManager accessManager, @NonNull String sessionKey);

    /**
     * Callback invoked after a token validation attempt finishes.
     *
     * @param tokenManager  The handler that performed the validation.
     * @param authException Null if validation succeeded; otherwise contains the failure reason (Expired/Invalid).
     */
    protected abstract void onValidAccessToken(@NonNull TokenManager tokenManager, @Nullable AuthException authException);

    /**
     * Registers an application as trusted based on its package name and optional certificate.
     *
     * @param packageName   The package name of the application to trust.
     * @param sha256Cert    The SHA-256 certificate fingerprint (required if extraSecurity is true).
     * @param extraSecurity If true, both package name and certificate must match during verification.
     */
    public void addTrustApp(@NonNull String packageName, @Nullable String sha256Cert, boolean extraSecurity) {
        if (extraSecurity){
            if (sha256Cert == null)
                return;
            trustedRequesterManager.addTrustAppWithCert(packageName, sha256Cert);
        } else {
            trustedRequesterManager.addTrustApp(packageName);
        }
    }

    /**
     * Revokes trust for a specific application.
     *
     * @param packageName The package name to remove from the whitelist.
     */
    public void removeTrustApp(@NonNull String packageName) {
        trustedRequesterManager.removeTrustApp(packageName);
    }

    /**
     * Toggles whether the library should generate and require session tokens.
     */
    public void setEnableCodeGeneration(boolean enableCodeGeneration) {
        this.enableCodeGeneration = enableCodeGeneration;
    }

    /**
     * Toggles whether the library should verify the caller's package identity.
     */
    public void setEnableTrustedApp(boolean enableTrustedApp) {
        this.enableTrustedApp = enableTrustedApp;
    }

    public void setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Verifies if the calling application's identity matches a trusted entry.
     *
     * @param packageName The package name of the caller to verify.
     * @return true if the caller is whitelisted and (if applicable) certificate matches.
     */
    public boolean isCallerTrusted(@NonNull String packageName) {
        if (applicationContext == null || packageName.isEmpty())
            return false;
        return trustedRequesterManager.isCallerTrusted(applicationContext, packageName);
    }

    /** @return true if session tokens are being generated for requests. */
    public boolean isEnableCodeGeneration() {
        return enableCodeGeneration;
    }

    /** @return true if caller identity verification is active. */
    public boolean isTrustedAppEnabled() {
        return enableTrustedApp;
    }

    /** Stores the Intent received from an external application. */
    public void setIncomingIntent(@NonNull Intent incomingIntent) {
        this.incomingIntent = incomingIntent;
    }

    /** Stores the payload intended for an outgoing response. */
    public void setResponseBody(@NonNull Bundle responseBody) {
        this.responseBody = responseBody;
    }

    /** Stores the final result data returned by a responder. */
    public void setResultBody(@Nullable Bundle resultBody) {
        this.resultBody = resultBody;
    }

    /**
     * Triggers the generation of a new secure session token.
     * <p>Successfully generated tokens trigger {@link #onAccessTokenGenerated(AccessManager, String)}.</p>
     */
    public void generateAccessToken() {
        if (!isEnableCodeGeneration()) {
            return;
        }
        tokenManager.generateToken();
        if (tokenManager.isTokenGenerated()) {
            auth.onAccessTokenGenerated(this.tokenManager);
        }
    }

    /**
     * Validates a received token string against the current session records.
     *
     * @param accessToken The token string to verify.
     */
    public void validateToken(@NonNull String accessToken) {
        if (!this.tokenManager.tokenExist(accessToken)){
            auth.onAccessTokenValidated(this.tokenManager, new AuthException(AuthException.TOKEN_INVALID, "The access token received is unknown."));
            return;
        }

        if (this.tokenManager.isValidToken(accessToken)) {
            auth.onAccessTokenValidated(this.tokenManager, null);
        } else {
            auth.onAccessTokenValidated(this.tokenManager,
                    new AuthException(AuthException.TOKEN_EXPIRED, "Access Token has expired."));
        }
    }

    /** @return The Intent that initiated the security check. */
    @NonNull
    public Intent getIncomingIntent() {
        return incomingIntent;
    }

    /** @return The most recently generated session token string. */
    @NonNull
    public String getSessionToken() {
        return tokenManager.getLastGeneratedKey();
    }

    /** @return The current response payload bundle. */
    public Bundle getResponseBody() {
        return responseBody;
    }

    /** @return The result data bundle, or null if no result was provided. */
    @Nullable
    public Bundle getResultBody() {
        return resultBody;
    }

    /** @return The human-readable message of the most recent security failure, if any. */
    @Nullable
    public String getErrorMessage(){
        return authException != null ? authException.getMessage() : null;
    }

    /** @return The error code of the most recent security failure. */
    public int getErrorCode(){
        return authException != null ? authException.getErrorCode() : 0;
    }
}