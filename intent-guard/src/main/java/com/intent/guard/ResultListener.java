package com.intent.guard;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.intent.guard.auth.AuthException;

/**
 * Callback interface for receiving the outcome of an outgoing intent request.
 * <p>
 * This listener provides a structured way to handle the asynchronous nature of
 * {@code startActivityForResult}. It separates successful data delivery from
 * various failure states like user cancellation or security token expiration.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public interface ResultListener {

    /**
     * Called when the target application successfully processes the request and
     * returns a valid payload.
     * <p>
     * If the exchange was secure, this method is only triggered after the
     * internal session token has been successfully validated.
     * </p>
     *
     * @param resultBody The data payload returned by the responding application.
     * May be {@code null} if the responding application finished
     * successfully but did not provide a result bundle.
     */
    void onResultReceived(@Nullable Bundle resultBody);

    /**
     * Called when the intent request fails to complete successfully.
     * <p>
     * This may occur due to manual user intervention (cancelling the dialog)
     * or technical security failures enforced by the library.
     * </p>
     *
     * @param reason The specific reason for failure. Corresponds to one of the
     * following constants:
     * <ul>
     * <li>{@link AuthException#USER_CANCELED}: User dismissed the request UI.</li>
     * <li>{@link AuthException#TOKEN_EXPIRED}: The session exceeded its validity period.</li>
     * <li>{@link AuthException#TOKEN_INVALID}: The returned token did not match the expected session.</li>
     * <li>{@link AuthException#INTERNAL_ERROR}: A system or reflection error occurred.</li>
     * </ul>
     */
    void onCancelled(int reason);
}