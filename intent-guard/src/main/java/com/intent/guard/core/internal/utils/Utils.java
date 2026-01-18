package com.intent.guard.core.internal.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;

import com.intent.guard.core.Metadata;

/**
 * Internal utility class for common library operations.
 * <p>
 * This class contains helper methods for context validation, string formatting,
 * and Intent classification. It is used to determine the security tier of
 * incoming requests based on the presence of specific metadata keys.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class Utils {

    /**
     * Checks if the provided context is an instance of an Activity.
     *
     * @param mContext The context to check.
     * @return {@code true} if the context is an Activity; {@code false} otherwise.
     */
    public static boolean isActivityDescendant(@NonNull Context mContext){
        return mContext instanceof Activity;
    }

    /**
     * Formats a string resource by replacing a placeholder with the requester's name.
     * <p>Specifically replaces the token {@code $requesterAppName} within the resource.</p>
     *
     * @param mContext The context to retrieve resources from.
     * @param resId    The string resource ID containing the placeholder.
     * @param name     The application name to inject.
     * @return The formatted string.
     */
    @NonNull
    public static String namePlacement(@NonNull Context mContext, @StringRes int resId, @NonNull String name){
        return mContext.getResources().getString(resId).replace("$requesterAppName", name);
    }

    /**
     * Null safety helper.
     *
     * @param object The object to check.
     * @return {@code true} if the object is null.
     */
    public static boolean isNull(@Nullable Object object){
        return object == null;
    }

    /**
     * Determines if an Intent is a standard IntentGuard request.
     * <p>A default request contains a body payload but lacks a security token.</p>
     *
     * @param requestIntent The incoming intent.
     * @return {@code true} if it matches the {@link com.intent.guard.RequestListener#DEFAULT_TYPE} criteria.
     */
    public static boolean isRequestDefault(@Nullable Intent requestIntent){
        return requestIntent != null
                && requestIntent.hasExtra(Metadata.REQUEST_BODY.getKey());
    }

    /**
     * Determines if an Intent is a secure IntentGuard request.
     * <p>A secure request must contain both a body payload and a validation token.</p>
     *
     * @param requestIntent The incoming intent.
     * @return {@code true} if it matches the {@link com.intent.guard.RequestListener#SECURE_TYPE} criteria.
     */
    public static boolean isRequestSecure(@Nullable Intent requestIntent){
        return requestIntent != null
                && requestIntent.hasExtra(Metadata.REQUEST_TOKEN.getKey())
                && requestIntent.hasExtra(Metadata.REQUEST_BODY.getKey());
    }

    /**
     * Ensures that the provided context is an Activity, throwing a RuntimeException if not.
     * <p>This is used for library operations that require a UI context (e.g., starting activities for results).</p>
     *
     * @param mContext The context to validate.
     * @param message  The error message to display if validation fails.
     * @return The casted {@link Activity} instance.
     * @throws RuntimeException if the context is not an Activity.
     */
    @NonNull
    public static Activity ensureIsActivityDescendant(@NonNull Context mContext, @NonNull String message){
        if (!isActivityDescendant(mContext)){
            throw new RuntimeException(message);
        }
        return (Activity) mContext;
    }
}