package com.intent.guard.core.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.intent.guard.ResultListener;
import com.intent.guard.auth.AuthException;
import com.intent.guard.core.access.AccessManager;
import com.intent.guard.core.token.TokenManager;

/**
 * The default implementation of {@link AccessManager} used by the IntentGuard library.
 * <p>
 * This class handles the standard security lifecycle for an Intent exchange. It listens for
 * token validation results and maps internal {@link AuthException} error codes to the
 * public {@link ResultListener} callbacks. It also ensures that session tokens are
 * consumed (invalidated) once a successful result is delivered to prevent replay attacks.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DefaultAccessManager extends AccessManager {

    @Nullable
    private ResultListener resultListener;

    /**
     * Constructs a new DefaultAccessManager with a default security level.
     * <p>The super constructor is called with a value of 5, defining the default
     * token complexity/security strength.</p>
     */
    public DefaultAccessManager(){
        super(5);
    }

    /**
     * Attaches a listener to receive the final security results.
     *
     * @param resultListener The listener that will handle the success or cancellation callbacks.
     */
    public void setResultListener(@Nullable ResultListener resultListener) {
        this.resultListener = resultListener;
    }

    /**
     * Called when a new access token is generated.
     * In the default implementation, this event is ignored as the token is managed internally.
     *
     * @param accessManager The current access manager instance.
     * @param sessionKey    The newly generated session key.
     */
    @Override
    protected void onAccessTokenGenerated(@NonNull AccessManager accessManager, @NonNull String sessionKey) {
        //Ignore.......
    }

    /**
     * Handles the outcome of a token validation attempt.
     * <p>
     * If an error occurred (e.g., token expired or invalid), it notifies the listener
     * with the specific {@link AuthException} code. If the token is valid, it consumes
     * the token to ensure single-use integrity and delivers the result body.
     * </p>
     *
     * @param tokenManager   The manager responsible for token lifecycle.
     * @param authException  The exception encountered during validation, or null if successful.
     */
    @Override
    protected void onValidAccessToken(@NonNull TokenManager tokenManager, @Nullable AuthException authException) {

        if (authException != null){
            switch (authException.getErrorCode()){
                case AuthException.USER_CANCELED:
                    if (resultListener != null) resultListener.onCancelled(AuthException.USER_CANCELED);
                    return;
                case AuthException.TOKEN_INVALID:
                    if (resultListener != null) resultListener.onCancelled(AuthException.TOKEN_INVALID);
                    return;
                case AuthException.TOKEN_EXPIRED:
                    if (resultListener != null) resultListener.onCancelled(AuthException.TOKEN_EXPIRED);
                    return;
            }
        }

        // Successfully validated: consume the token to prevent reuse
        tokenManager.consumeToken(getSessionToken());

        // Return the response data to the calling application
        if (resultListener != null) resultListener.onResultReceived(getResultBody());
    }
}