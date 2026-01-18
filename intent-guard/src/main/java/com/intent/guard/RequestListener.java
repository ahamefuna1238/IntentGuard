package com.intent.guard;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.intent.guard.core.Metadata;

/**
 * Callback interface for listening to incoming intent requests intercepted by IntentGuard.
 * <p>
 * This listener categorizes incoming intents into three security tiers:
 * {@link #UNKNOWN_TYPE}, {@link #DEFAULT_TYPE}, and {@link #SECURE_TYPE}.
 * Developers should implement this interface to define how their application
 * responds to data based on these trust levels.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public interface RequestListener {

    /**
     * Indicates that the incoming intent does not follow the IntentGuard protocol.
     * <p>
     * This type is returned when required metadata, such as {@link Metadata#REQUEST_BODY}
     * or {@link Metadata#REQUEST_TOKEN}, is missing. This usually applies to standard
     * Android system intents like {@link Intent#ACTION_SEND} or generic third-party intents.
     * </p>
     */
    int UNKNOWN_TYPE = 0;

    /**
     * Indicates a standard IntentGuard request that contains data but no security token.
     * <p>
     * This type is returned when the intent contains {@link Metadata#REQUEST_BODY}
     * but lacks a {@link Metadata#REQUEST_TOKEN}. It suggests a structured request
     * without explicit session validation.
     * </p>
     */
    int DEFAULT_TYPE = 1;

    /**
     * Indicates a fully authenticated IntentGuard request.
     * <p>
     * This type is returned when the intent contains both {@link Metadata#REQUEST_TOKEN}
     * and {@link Metadata#REQUEST_BODY}. This represents the highest level of trust,
     * confirming the request has passed session validation.
     * </p>
     */
    int SECURE_TYPE = 2;

    /**
     * Called when a new request is intercepted and classified by the library.
     * <p>
     * Implementation of this method should switch on the {@code intentType} to
     * determine the level of access or the type of processing required for the
     * provided {@link Intent}.
     * </p>
     *
     * @param intent     The raw incoming intent containing the payload.
     * @param intentType The security classification: {@link #UNKNOWN_TYPE},
     * {@link #DEFAULT_TYPE}, or {@link #SECURE_TYPE}.
     */
    void onRequestReceived(@NonNull Intent intent, int intentType);
}