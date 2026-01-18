package com.intent.guard.request;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.intent.guard.RequestListener;
import com.intent.guard.core.Metadata;

import java.util.ArrayList;

/**
 * A helper class for building structured {@link Intent} requests that can be consumed by other applications
 * via {@link RequestListener#onRequestReceived(Intent, int)}.
 *
 * <p>
 * The {@code IntentRequest} wraps a standard Android {@link Intent} and provides fluent, typed methods
 * (such as {@link #putString(String, String)}, {@link #putFloat(String, float)}, etc.) to safely attach
 * request data into a managed {@link Bundle}.
 * </p>
 *
 * <h3>How it works</h3>
 * <ul>
 * <li>All request data is stored inside a nested {@link Bundle} attached to the Intent using
 * {@link Metadata#REQUEST_BODY} as the key.</li>
 * <li>If a key already exists in the request body, calling a {@code put} method with the same key
 * will overwrite the previous value.</li>
 * <li>{@link #ensureBundle()} guarantees the request body always exists. If the Intent has no bundle,
 * a new one is created automatically.</li>
 * </ul>
 *
 * <h3>Permissions</h3>
 * You can define a set of "request permissions" (data fields the receiver is expected to provide back or
 * acknowledge) by calling {@link #setRequestPermissions(ArrayList)}. This is used by the receiving
 * application to show a rationale UI to the user.
 *
 * <h3>Example</h3>
 * <pre>{@code
 *
 * IntentRequest intentRequest = new IntentRequest(new Intent())
 *        .putString("com.webhook.url", "https://api.service.com/hook")
 *        .putDouble("com.transaction.amount", 252.99)
 *        .putBoolean("com.card.payment", true);
 *
 * ArrayList<String> permissions = new ArrayList<>();
 * permissions.add("com.transaction.amount");
 * permissions.add("com.card.payment");
 *
 * intentRequest.setRequestPermissions(permissions);
 *
 * IntentGuardManager manager = new IntentGuardManager(this,null);
 * manager.sendRequest(intentRequest);
 * }</pre>
 *
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Use the provided {@code putX()} methods instead of directly modifying Intent extras to ensure
 * the payload stays within the library's expected structure.</li>
 * <li>Requests are one-time use; do not reuse the same {@code IntentRequest} object for
 * multiple transactions.</li>
 * </ul>
 *
 * @since 1.0
 * @author David Onyia
 */
public final class IntentRequest {

    private final Intent managedIntent;

    /**
     * Creates a new request wrapper around a raw Intent.
     * @param intent The base Intent to be sent.
     */
    public IntentRequest(@NonNull Intent intent){
        this.managedIntent = intent;
    }

    /**
     * Manually sets the request body bundle.
     * <p>Warning: This replaces any data previously added via {@code putX()} methods.</p>
     * @param bundle The bundle containing the request payload.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest setRequestBody(Bundle bundle){
        managedIntent.putExtra(Metadata.REQUEST_BODY.getKey(), bundle);
        return this;
    }

    /**
     * Adds a String to the request payload.
     * @param key   The request key.
     * @param value The String value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest putString(@NonNull String key, @NonNull String value){
        ensureBundle();
        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBundle.putString(key, value);
        return this;
    }

    /**
     * Adds an integer to the request payload.
     * @param key   The request key.
     * @param value The integer value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest putInt(@NonNull String key, int value){
        ensureBundle();
        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBundle.putInt(key, value);
        return this;
    }

    /**
     * Adds a boolean to the request payload.
     * @param key   The request key.
     * @param value The boolean value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest putBoolean(@NonNull String key, boolean value){
        ensureBundle();
        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBundle.putBoolean(key, value);
        return this;
    }

    /**
     * Adds a double to the request payload.
     * @param key   The request key.
     * @param value The double value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest putDouble(@NonNull String key, double value){
        ensureBundle();
        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBundle.putDouble(key, value);
        return this;
    }

    /**
     * Adds a long to the request payload.
     * @param key   The request key.
     * @param value The long value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest putLong(@NonNull String key, long value){
        ensureBundle();
        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBundle.putLong(key, value);
        return this;
    }

    /**
     * Adds a float to the request payload.
     * @param key   The request key.
     * @param value The float value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest putFloat(@NonNull String key, float value){
        ensureBundle();
        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBundle.putFloat(key, value);
        return this;
    }

    /**
     * Adds a nested Bundle to the request payload.
     * @param key   The request key.
     * @param value The bundle value.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    public IntentRequest putBundle(@NonNull String key, @Nullable Bundle value){
        ensureBundle();
        Bundle requestBody = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        requestBody.putBundle(key, value);
        return this;
    }

    /**
     * Attaches a list of permission identifiers to the request.
     * <p>These represent the data fields the receiver is authorized to access or
     * must acknowledge via user interaction.</p>
     * @param requestPermissions List of permission strings.
     * @return The current {@link IntentRequest} instance for chaining.
     */
    @NonNull
    public IntentRequest setRequestPermissions(@NonNull ArrayList<String> requestPermissions){
        managedIntent.putExtra(Metadata.REQUEST_PERMISSIONS.getKey(), requestPermissions);
        return this;
    }

    /**
     * Internal utility to ensure the underlying Intent has a valid Request Body Bundle attached.
     */
    private void ensureBundle() {
        if (!managedIntent.hasExtra(Metadata.REQUEST_BODY.getKey())) {
            managedIntent.putExtra(Metadata.REQUEST_BODY.getKey(), new Bundle());
            return;
        }

        Bundle requestBundle = managedIntent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
        if (requestBundle == null){
            managedIntent.putExtra(Metadata.REQUEST_BODY.getKey(), new Bundle());
        }
    }

    /**
     * Returns the underlying Android Intent containing the structured payload.
     * <p>This is restricted for use within the library to facilitate the sending process.</p>
     * @return The configured Intent.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @NonNull
    public Intent getIntent() {
        return managedIntent;
    }
}