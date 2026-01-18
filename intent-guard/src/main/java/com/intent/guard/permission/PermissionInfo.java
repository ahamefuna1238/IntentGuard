package com.intent.guard.permission;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intent.guard.request.RequestPermission;

import java.util.Objects;

/**
 * Data container for defining user-friendly metadata for a security permission.
 * <p>
 * This class stores the visual and textual information displayed to the user when
 * the {@link RequestPermission} component triggers a rationale dialog. It allows
 * developers to explain <i>why</i> a specific permission is being requested in
 * plain language, accompanied by an illustrative icon.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public final class PermissionInfo {

    /** Constant indicating that no custom icon has been assigned to this permission. */
    public static final int NO_IMAGE_SET = 0;

    private final int permissionImage;
    private final String permissionText;

    /**
     * Constructs a PermissionInfo with a text description only.
     *
     * @param permissionText Human-readable explanation of the permission.
     */
    public PermissionInfo(String permissionText){
        this(NO_IMAGE_SET, permissionText);
    }

    /**
     * Constructs a PermissionInfo with a custom icon and text description.
     *
     * @param permissionImage A drawable resource ID (e.g., R.drawable.ic_camera).
     * @param permissionText  Human-readable explanation of the permission.
     */
    public PermissionInfo(@DrawableRes int permissionImage, String permissionText){
        this.permissionImage = permissionImage;
        this.permissionText = permissionText;
    }

    /** @return The drawable resource ID for the permission icon. */
    public int getPermissionImage() {
        return permissionImage;
    }

    /** @return The human-readable string describing the permission. */
    public String getPermissionText() {
        return permissionText;
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionImage, permissionText);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PermissionInfo other = (PermissionInfo) obj;
        return permissionImage == other.permissionImage
                && Objects.equals(permissionText, other.permissionText);
    }

    /**
     * Entry point for the fluent {@link Builder} API.
     *
     * @return A new Builder instance.
     */
    @NonNull
    public static Builder builder(){
        return new Builder();
    }

    /**
     * A fluent builder class for creating {@link PermissionInfo} instances.
     */
    public static final class Builder {

        private int permissionImage = NO_IMAGE_SET;
        private String permissionText;

        private Builder(){
        }

        /**
         * Attaches a visual icon to the permission info.
         *
         * @param permissionImage A {@link DrawableRes} ID.
         * @return The builder instance for chaining.
         */
        @NonNull
        public Builder setImage(@DrawableRes int permissionImage){
            this.permissionImage = permissionImage;
            return this;
        }

        /**
         * Sets the rationale text to be displayed to the user.
         *
         * @param permissionText A descriptive string.
         * @return The builder instance for chaining.
         */
        @NonNull
        public Builder setText(@NonNull String permissionText){
            this.permissionText = permissionText;
            return this;
        }

        /**
         * Validates and creates the final {@link PermissionInfo} object.
         *
         * @return A configured PermissionInfo instance.
         */
        @NonNull
        public PermissionInfo build(){
            return new PermissionInfo(permissionImage, permissionText);
        }
    }
}