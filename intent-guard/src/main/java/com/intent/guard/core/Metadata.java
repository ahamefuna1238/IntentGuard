package com.intent.guard.core;

import androidx.annotation.NonNull;

import com.intent.guard.request.IntentRequest;

import java.util.ArrayList;

/**
 * This consist of keys used to hold various data type (e.g request body, response body,...e.t.c)
 * in a bundle or intent which can be found in {@link IntentRequest}
 * @since 1.0
 * @author David Onyia
 */
public enum Metadata {

    /**
     * Holds the list of required request permissions define via {@link IntentRequest#setRequestPermissions(ArrayList)}
     */
    REQUEST_PERMISSIONS("com.android.intent.request_permissions"),
    /**
     * Holds the default (non-secure) response body returned by a receiving application when the request does not include a security token.
     */
    DEFAULT_RESPONSE_BODY("com.android.intent.default_response_body"),
    /**
     * Holds the secure response body returned by a receiving application when the request includes a security token.
     */
    RESPONSE_BODY("com.android.intent.response_body"),
    /**
     * Holds the request body.
     */
    REQUEST_BODY("com.android.intent.request_body"),
    /**
     * Holds the request token, if the request is secured and requires authentication.
     */
    REQUEST_TOKEN("com.android.intent.request_token");

    private final String key;

    Metadata(String key) {
        this.key = key;
    }

    /**
     * Returns the actual key string used in intents.
     */
    @NonNull
    public String getKey() {
        return key;
    }

    /**
     * Returns a string representation for debugging.
     * @return A string in the form {@code Metadata{keys}}
     */
    @NonNull
    public String toString() {
        return "Metadata{" + key + "}";
    }
}
