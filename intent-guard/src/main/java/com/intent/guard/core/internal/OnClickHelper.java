package com.intent.guard.core.internal;

import static android.app.Activity.RESULT_CANCELED;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;

import com.intent.guard.RequestListener;
import com.intent.guard.core.access.AccessManager;
import com.intent.guard.core.internal.utils.Utils;

/**
 * Internal listener responsible for handling click events on the authorization dialog action buttons.
 * <p>
 * This helper manages the transitions between the UI layer and the logic layer. When a user interacts
 * with the dialog, this class dismisses the UI and either finishes the host component with a
 * canceled result or triggers the secure request processing flow via the {@link RequestListener}.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class OnClickHelper implements View.OnClickListener {

    /** Identifier for the 'Cancel' or 'Deny' action button. */
    public static final int CANCEL_BUTTON = 0;

    /** Identifier for the 'Approve' or 'Allow' action button. */
    public static final int APPROVE_BUTTON = 1;

    private final int mButton;
    private final AccessManager mAccessManager;
    private final AuthDialogBuilder mAuthDialogBuilder;
    private final Activity mActivity;
    private final Fragment mFragment;
    private RequestListener mRequestListener;

    /**
     * Constructs a click helper for Fragment-hosted dialogs.
     * @param mButton           The type of button.
     * @param mFragment           The parent Fragment.
     * @param mAccessManager      The security manager holding the intent data.
     * @param mAuthDialogBuilder  The builder instance managing the dialog visibility.
     */
    public OnClickHelper(int mButton, @NonNull Fragment mFragment, @NonNull AccessManager mAccessManager, @NonNull AuthDialogBuilder mAuthDialogBuilder){
        this(mButton, null, mFragment, mAccessManager, mAuthDialogBuilder);
    }

    /**
     * Constructs a click helper for Activity-hosted dialogs.
     * @param mButton           The type of button.
     * @param mContext            The context, which must be a descendant of an Activity.
     * @param mAccessManager      The security manager holding the intent data.
     * @param mAuthDialogBuilder  The builder instance managing the dialog visibility.
     */
    public OnClickHelper(int mButton, @NonNull Context mContext, @NonNull AccessManager mAccessManager, @NonNull AuthDialogBuilder mAuthDialogBuilder){
        this(mButton, mContext, null, mAccessManager, mAuthDialogBuilder);
    }

    /**
     * Internal master constructor for OnClickHelper.
     */
    private OnClickHelper(int mButton, @Nullable Context mContext, @Nullable Fragment mFragment, @NonNull AccessManager mAccessManager, @NonNull AuthDialogBuilder mAuthDialogBuilder){
        this.mButton = mButton;
        this.mActivity = mContext != null ? Utils.ensureIsActivityDescendant(mContext,
                mContext.getClass().getSimpleName()
                        + " cannot be used in OnClickHelper use an activity instead.") : null;
        this.mFragment = mFragment;
        this.mAccessManager = mAccessManager;
        this.mAuthDialogBuilder = mAuthDialogBuilder;
    }

    /**
     * Sets the listener to be notified when the "Approve" action is triggered.
     * * @param mRequestListener The listener to receive the secure Intent request.
     */
    public void setRequestListener(@Nullable RequestListener mRequestListener) {
        this.mRequestListener = mRequestListener;
    }

    /**
     * Handles the click event.
     * <p>
     * First, it dismisses the dialog if showing.
     * If {@link #CANCEL_BUTTON} was clicked, it sets the Activity result to CANCELED and finishes the host.
     * If {@link #APPROVE_BUTTON} was clicked, it notifies the {@link RequestListener} that
     * a secure request is ready for processing.
     * </p>
     */
    @Override
    public void onClick(View v) {

        if (mAuthDialogBuilder.isShowing()){
            mAuthDialogBuilder.dismissAuthDialog();
        }

        if (mButton == CANCEL_BUTTON){
            if (mActivity != null) {
                mActivity.setResult(RESULT_CANCELED, null);
                mActivity.finish();
            } else {
                // Defensive check: handle Fragment lifecycle
                if (mFragment != null){
                    // We call the activity holding the fragment to properly deliver the result.
                    Activity mParentActivity = mFragment.getActivity();

                    if (mParentActivity != null){
                        mParentActivity.setResult(RESULT_CANCELED, null);
                        mParentActivity.finish();
                    }
                }
            }
        } else if (mButton == APPROVE_BUTTON) {
            if (mRequestListener != null) {
                // Notify the application that the user has securely approved the request
                mRequestListener.onRequestReceived(mAccessManager.getIncomingIntent(), RequestListener.SECURE_TYPE);
            }
        }
    }
}