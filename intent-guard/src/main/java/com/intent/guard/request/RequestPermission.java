package com.intent.guard.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intent.guard.permission.PermissionInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton registry for managing {@link PermissionInfo} objects associated with unique permission keys.
 * <p>
 * This class serves as the knowledge base for the library's UI components. When an incoming
 * intent requests specific data (e.g., "com.user.profile"), the library looks up the corresponding
 * {@link PermissionInfo} here to display a human-readable description and icon to the user.
 * </p>
 *
 * <h3>Key Responsibilities</h3>
 * <ul>
 * <li>Register custom descriptions for permission keys.</li>
 * <li>Provide a thread-safe lookup mechanism for the UI layer.</li>
 * <li>Prevent accidental overwriting of sensitive permission metadata.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *    // Register a permission with an icon and description
 *    RequestPermission.getInstance()
 *           .definePermissionInfo("com.user.email",
 *                new PermissionInfo(R.drawable.ic_email, "Access your verified email address"));
 *
 *    // Alternatively, use the Builder pattern
 *    PermissionInfo info = PermissionInfo.builder()
 *             .setImage(R.drawable.ic_profile)
 *             .setText("Access your display name and photo")
 *              .build();
 *
 *    RequestPermission.getInstance()
 *              .definePermissionInfo("com.user.profile", info);
 * }</pre>
 *
 * @since 1.0
 * @author David Onyia
 */
public final class RequestPermission {

    private static volatile RequestPermission instance;

    /** * Synchronized map to ensure thread safety during concurrent read/write operations.
     * Key: The unique permission identifier (e.g., "com.app.data").
     * Value: The metadata describing that permission.
     */
    private final Map<String, PermissionInfo> permissionInfoHashMap
            = Collections.synchronizedMap(new HashMap<>());

    private RequestPermission(){
    }

    /**
     * Returns the singleton instance of {@link RequestPermission} using double-checked locking.
     * * @return The shared registry instance.
     */
    @NonNull
    public static RequestPermission getInstance() {
        if (instance == null){
            synchronized (RequestPermission.class){
                if (instance == null) {
                    instance = new RequestPermission();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a new {@link PermissionInfo} for a specific key.
     * <p>
     * <b>Note:</b> To maintain integrity, this method will not overwrite an existing key.
     * If you need to update a description, you must first call {@link #removePermissionInfo(String)}.
     * </p>
     *
     * @param permissionKey  A unique identifier (e.g., {@code "com.user.identity"}).
     * @param permissionInfo The metadata (text and image) for the permission.
     * @return This instance for method chaining.
     */
    public RequestPermission definePermissionInfo(@NonNull String permissionKey, @NonNull PermissionInfo permissionInfo){
        if (isValidEntry(permissionKey)){
            permissionInfoHashMap.put(permissionKey, permissionInfo);
        }
        return this;
    }

    /**
     * Removes the mapping for a given permission key.
     *
     * @param permissionKey The identifier to remove from the registry.
     */
    public void removePermissionInfo(@NonNull String permissionKey){
        if (isValidExit(permissionKey)){
            permissionInfoHashMap.remove(permissionKey);
        }
    }

    /**
     * Retrieves metadata for a permission key.
     *
     * @param permissionKey The key found in an incoming {@link IntentRequest}.
     * @return The associated {@link PermissionInfo}, or {@code null} if the key is unregistered.
     */
    @Nullable
    public PermissionInfo getDefinePermissionInfo(@NonNull String permissionKey){
        if (isValidExit(permissionKey)){
            return permissionInfoHashMap.get(permissionKey);
        }
        return null;
    }

    /**
     * Validates that the key is not empty and doesn't already exist in the map.
     */
    private boolean isValidEntry(@NonNull String permissionKey){
        return !permissionKey.isEmpty() && !permissionInfoHashMap.containsKey(permissionKey);
    }

    /**
     * Validates that the key is not empty and exists in the map.
     */
    private boolean isValidExit(@NonNull String permissionKey){
        return !permissionKey.isEmpty() && permissionInfoHashMap.containsKey(permissionKey);
    }
}