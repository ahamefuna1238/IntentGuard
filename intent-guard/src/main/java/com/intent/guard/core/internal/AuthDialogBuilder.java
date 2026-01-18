package com.intent.guard.core.internal;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.intent.guard.R;
import com.intent.guard.core.DialogType;

/**
 * Internal UI engine responsible for abstracting the authorization interface.
 * <p>
 * AuthDialogBuilder provides a unified API to manage both {@link BottomSheetDialog}
 * (standard window-based) and {@link BottomSheetDialogFragment} (lifecycle-aware).
 * It handles the structural differences in view lookup and lifecycle management
 * between Activity-hosted and Fragment-hosted authorization flows.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class AuthDialogBuilder {

    /** Constant used to indicate that no custom theme should be applied to the dialog. */
    public static final int NO_THEME = -1;

    private final boolean isDefaultLayoutInUse;
    private DialogType mDefaultType = DialogType.DEFAULT;

    private BottomSheetDialog mAuthDialog;
    private BottomSheetDialogFragment mAuthDialogFragment;

    @Nullable
    private final FragmentManager fragmentManager;

    @Nullable
    private Button cancelButton;

    @Nullable
    private Button approveButton;

    /**
     * Constructs a builder for Activity-based authorization flows.
     *
     * @param mContext      The parent Activity context.
     * @param canDismiss    Whether the dialog can be dismissed by clicking outside or pressing back.
     * @param mAuthLayout   The layout resource ID to inflate.
     * @param dialogTheme   The theme resource ID (use {@link #NO_THEME} for default).
     * @param dialogType    The strategy to use (DEFAULT for Dialog, FRAGMENT for DialogFragment).
     */
    public AuthDialogBuilder(@NonNull Context mContext, boolean canDismiss, @LayoutRes int mAuthLayout, @StyleRes int dialogTheme, @Nullable DialogType dialogType){
        this(mContext, null, canDismiss, mAuthLayout, dialogTheme, dialogType);
    }

    /**
     * Constructs a builder for Fragment-based authorization flows.
     *
     * @param mFragmentManager The FragmentManager used to display the {@link BottomSheetDialogFragment}.
     * @param canDismiss       Whether the fragment dialog is cancelable.
     * @param mAuthLayout      The layout resource ID to inflate.
     * @param dialogTheme      The theme resource ID.
     * @param dialogType       The strategy to use.
     */
    public AuthDialogBuilder(@NonNull FragmentManager mFragmentManager, boolean canDismiss, @LayoutRes int mAuthLayout, @StyleRes int dialogTheme, @Nullable DialogType dialogType){
        this(null, mFragmentManager, canDismiss, mAuthLayout, dialogTheme, dialogType);
    }

    /**
     * Internal master constructor that initializes either a Dialog or a Fragment based on the provided params.
     */
    private AuthDialogBuilder(@Nullable Context mContext, @Nullable FragmentManager mFragmentManager, boolean canDismiss, @LayoutRes int mAuthLayout, @StyleRes int dialogTheme, @Nullable DialogType dialogType){
        this.fragmentManager = mFragmentManager;
        this.isDefaultLayoutInUse = mAuthLayout == R.layout.auth_bottom_dialog_layout;

        if (dialogType != null){
            this.mDefaultType = dialogType;
        }

        if (isDefaultType() && mContext != null){
            if (hasTheme(dialogTheme)){
                mAuthDialog = new BottomSheetDialog(mContext, dialogTheme);
            } else {
                mAuthDialog = new BottomSheetDialog(mContext);
            }

            mAuthDialog.setCancelable(canDismiss);
            mAuthDialog.setCanceledOnTouchOutside(canDismiss);
            mAuthDialog.setContentView(mAuthLayout);
        } else {
            mAuthDialogFragment = new BottomSheetDialogFragment(mAuthLayout);
            mAuthDialogFragment.setCancelable(canDismiss);
            mAuthDialogFragment.setShowsDialog(true);
        }
    }

    /**
     * Finds and registers the cancel button from the layout and attaches the provided click listener.
     *
     * @param cancelId        The resource ID of the cancel button.
     * @param onClickListener The callback for the click event.
     */
    public void registerCancelButton(@IdRes int cancelId, @NonNull View.OnClickListener onClickListener){
        if (isDefaultType()){
            if (mAuthDialog != null){
                cancelButton = mAuthDialog.findViewById(cancelId);
                if (cancelButton != null){
                    cancelButton.setOnClickListener(onClickListener);
                }
            }
        } else {
            if (mAuthDialogFragment != null){
                View mFragmentDialogView = mAuthDialogFragment.getView();
                if (mFragmentDialogView != null){
                    cancelButton = mFragmentDialogView.findViewById(cancelId);
                    cancelButton.setOnClickListener(onClickListener);
                }
            }
        }
    }

    /**
     * Finds and registers the approve button from the layout and attaches the provided click listener.
     *
     * @param approveId       The resource ID of the approve button.
     * @param onClickListener The callback for the click event.
     */
    public void registerApproveButton(@IdRes int approveId, @NonNull View.OnClickListener onClickListener){
        if (isDefaultType()){
            if (mAuthDialog != null){
                approveButton = mAuthDialog.findViewById(approveId);
                if (approveButton != null){
                    approveButton.setOnClickListener(onClickListener);
                }
            }
        } else {
            if (mAuthDialogFragment != null){
                View mFragmentDialogView = mAuthDialogFragment.getView();
                if (mFragmentDialogView != null){
                    approveButton = mFragmentDialogView.findViewById(approveId);
                    approveButton.setOnClickListener(onClickListener);
                }
            }
        }
    }

    /**
     * Sets a custom theme for the dialog context.
     * @param dialogTheme The style resource ID.
     */
    public void setTheme(@StyleRes int dialogTheme){
        if (hasTheme(dialogTheme)){
            if (mAuthDialog != null){
                mAuthDialog.getContext().setTheme(dialogTheme);
            }
        }
    }

    /**
     * Updates the cancelable state of the dialog or fragment.
     * @param cancelAble If true, the UI can be dismissed by the user.
     */
    public void setCancelAble(boolean cancelAble){
        if (isDefaultType()){
            if (mAuthDialog != null){
                mAuthDialog.setCancelable(cancelAble);
                mAuthDialog.setCanceledOnTouchOutside(cancelAble);
            }
        } else {
            if (mAuthDialogFragment != null){
                mAuthDialogFragment.setCancelable(cancelAble);
            }
        }
    }

    /**
     * Performs a type-safe view lookup within the current UI component.
     * * @param resId The ID of the view to find.
     * @return The View of type T, or null if not found or UI is not prepared.
     */
    @Nullable
    public <T extends View> T findViewById(@IdRes int resId){
        if (isDefaultType()){
            if (mAuthDialog != null){
                return mAuthDialog.findViewById(resId);
            }
        } else {
            if (mAuthDialogFragment != null){
                View mFragmentDialogView = mAuthDialogFragment.getView();
                if (mFragmentDialogView != null){
                    return mFragmentDialogView.findViewById(resId);
                }
            }
        }
        return null;
    }

    /**
     * Checks if both the approve and cancel buttons have been successfully identified and registered.
     * @return true if both buttons are non-null.
     */
    public boolean areAllButtonsRegistered(){
        return cancelButton != null && approveButton != null;
    }

    /**
     * @return true if the library's default layout resource is being used.
     */
    public boolean isDefaultLayoutInUse() {
        return isDefaultLayoutInUse;
    }

    /**
     * Checks if the authorization UI is currently visible on the screen.
     * @return true if showing.
     */
    public boolean isShowing(){
        if (isDefaultType()){
            if (mAuthDialog != null){
                return mAuthDialog.isShowing();
            }
        } else {
            if (mAuthDialogFragment != null){
                Dialog mDialog = mAuthDialogFragment.getDialog();
                if (mDialog != null){
                    return mDialog.isShowing();
                } else {
                    return mAuthDialogFragment.isAdded();
                }
            }
        }
        return false;
    }

    /**
     * Triggers the display of the authorization dialog.
     * If a DialogFragment is used, it utilizes the {@link FragmentManager} provided during construction.
     */
    public void showAuthDialog(){
        if (isShowing()){
            dismissAuthDialog();
        }

        if (isDefaultType()){
            if (mAuthDialog != null){
                mAuthDialog.show();
            }
        } else {
            if (mAuthDialogFragment != null){
                if (fragmentManager != null){
                    mAuthDialogFragment.show(fragmentManager, "auth_fragment_dialog");
                }
            }
        }
    }

    /**
     * Dismisses the authorization UI.
     * Includes safety checks to ensure the UI is currently active before attempting dismissal.
     */
    public void dismissAuthDialog(){
        if (isDefaultType()){
            if (mAuthDialog != null){
                if (mAuthDialog.isShowing()){
                    mAuthDialog.dismiss();
                }
            }
        } else {
            if (mAuthDialogFragment != null){
                if (mAuthDialogFragment.isAdded()){
                    mAuthDialogFragment.dismiss();
                }
            }
        }
    }

    private boolean hasTheme(@StyleRes int theme){
        return theme != NO_THEME;
    }

    private boolean isDefaultType(){
        return mDefaultType == DialogType.DEFAULT;
    }
}