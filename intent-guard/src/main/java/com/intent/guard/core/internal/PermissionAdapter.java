package com.intent.guard.core.internal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.RecyclerView;

import com.intent.guard.R;
import com.intent.guard.permission.PermissionInfo;

import java.util.List;

/**
 * Internal RecyclerView adapter responsible for displaying the list of requested permissions.
 * <p>
 * This adapter supports two distinct view types:
 * <ul>
 * <li><b>Standard:</b> Displays only the permission text.</li>
 * <li><b>Iconified:</b> Displays both an icon and the permission text.</li>
 * </ul>
 * It dynamically selects the appropriate layout based on whether a valid image resource
 * is provided in the {@link PermissionInfo} object.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    /** Constant representing a view type that includes an image/icon. */
    private static final int HAS_IMAGE = 1;

    private final List<PermissionInfo> permissionInfoList;

    /**
     * Constructs the adapter with a list of permission data models.
     * @param permissionInfoList The list of permissions to be displayed in the dialog.
     */
    public PermissionAdapter(List<PermissionInfo> permissionInfoList){
        this.permissionInfoList = permissionInfoList;
    }

    /**
     * Inflates the appropriate XML layout based on the view type (with or without image).
     * @param parent   The parent ViewGroup.
     * @param viewType The type of view to inflate (0 for no image, 1 for image).
     * @return A new ViewHolder instance holding the inflated view.
     */
    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        if (viewType == PermissionInfo.NO_IMAGE_SET){
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.auth_permission_item_layout_without_image, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.auth_permission_item_layout_with_image, parent, false);
        }

        return new PermissionViewHolder(itemView, viewType);
    }

    /**
     * Binds the permission data (text and optionally the icon) to the ViewHolder.
     * @param holder   The ViewHolder to update.
     * @param position The position of the item within the list.
     */
    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        PermissionInfo permissionInfo = permissionInfoList.get(position);

        if (holder.getItemType() == HAS_IMAGE) {
            holder.permissionImage.setBackgroundResource(permissionInfo.getPermissionImage());
        }

        holder.permissionText.setText(permissionInfo.getPermissionText());
    }

    /**
     * Determines the view type for the item at the specified position.
     * @param position The list position.
     * @return {@link #HAS_IMAGE} if an icon is set, otherwise 0.
     */
    @Override
    public int getItemViewType(int position) {
        PermissionInfo permissionInfo = permissionInfoList.get(position);

        if (permissionInfo.getPermissionImage() == PermissionInfo.NO_IMAGE_SET){
            return 0;
        } else {
            return HAS_IMAGE;
        }
    }

    /**
     * @return The total number of permissions in the list.
     */
    @Override
    public int getItemCount() {
        return permissionInfoList.size();
    }

    /**
     * ViewHolder class that caches view references for the permission list items.
     */
    public static class PermissionViewHolder extends RecyclerView.ViewHolder {

        int itemType;
        ImageView permissionImage;
        TextView permissionText;

        /**
         * Initializes the view references based on the provided item view type.
         * @param itemView     The inflated view.
         * @param itemViewType The type of the view (HAS_IMAGE or not).
         */
        public PermissionViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);
            this.itemType = itemViewType;

            permissionText = itemView.findViewById(R.id.permissionText);

            if (itemViewType == HAS_IMAGE){
                permissionImage = itemView.findViewById(R.id.permissionImage);
            }
        }

        /**
         * @return The view type of this specific ViewHolder.
         */
        public int getItemType() {
            return itemType;
        }
    }
}