package com.intent.guard.auth;

import androidx.annotation.NonNull;

import com.intent.guard.ResultListener;

/**
 * Specialized exception thrown during the authentication and token validation lifecycle.
 * <p>
 * {@code AuthException} encapsulates failures related to secure session management.
 * It is primarily utilized within the validation callbacks to signal why a
 * request-response cycle failed. The associated error codes are mapped to the
 * public {@link ResultListener#onCancelled(int)} method to provide developers
 * with actionable feedback.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public class AuthException extends Exception {

    /**
     * Error code indicating an unrecoverable internal library error.
     * <p>Occurs during unexpected failures such as reflection errors or system-level crashes.</p>
     */
    public static final int INTERNAL_ERROR = -111;

    /**
     * Error code indicating that the session token's validity period has lapsed.
     * <p>The token was presented after its expiration timestamp as defined by the security policy.</p>
     */
    public static final int TOKEN_EXPIRED = -112;

    /**
     * Error code indicating that the provided token does not match the expected session key.
     * <p>This may signal a potential tampering attempt or an out-of-sync session state.</p>
     */
    public static final int TOKEN_INVALID = -113;

    /**
     * Error code indicating that the user manually dismissed the authorization UI.
     * <p>This occurs when the user clicks the "Cancel" button or dismisses the bottom sheet dialog.</p>
     */
    public static final int USER_CANCELED = -114;

    private final int errorCode;

    /**
     * Constructs a new AuthException with a specific error code and description.
     *
     * @param errorCode One of the defined static error constants.
     * @param message   A human-readable description of the error.
     */
    public AuthException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new AuthException with a specific error code, description, and cause.
     *
     * @param errorCode One of the defined static error constants.
     * @param message   A human-readable description of the error.
     * @param cause     The underlying throwable that triggered this exception.
     */
    public AuthException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Retrieves the specific error code associated with this exception.
     *
     * @return An integer representing the failure type (e.g., {@link #TOKEN_EXPIRED}).
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns a string representation of the exception including the error code.
     */
    @NonNull
    @Override
    public String toString() {
        return "AuthException{" +
                "errorCode=" + errorCode +
                ", message=" + getMessage() +
                '}';
    }
}