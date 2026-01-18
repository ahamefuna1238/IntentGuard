package com.intent.guard;

import static com.intent.guard.RequestListener.DEFAULT_TYPE;
import static com.intent.guard.RequestListener.UNKNOWN_TYPE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.intent.guard.auth.AuthException;
import com.intent.guard.core.DialogType;
import com.intent.guard.core.Metadata;
import com.intent.guard.core.access.AccessManager;
import com.intent.guard.core.internal.AuthDialogBuilder;
import com.intent.guard.core.internal.DefaultAccessManager;
import com.intent.guard.core.internal.IntentProcessor;
import com.intent.guard.core.internal.OnClickHelper;
import com.intent.guard.core.internal.PermissionAdapter;
import com.intent.guard.core.internal.utils.Utils;
import com.intent.guard.permission.PermissionInfo;
import com.intent.guard.request.IntentRequest;

import java.util.List;

/**
 * The central orchestration component for the IntentGuard library.
 * <p>
 * {@code IntentGuardManager} acts as a Facade, coordinating communication between the security layer,
 * the UI rationale layer, and the intent processing layer. It handles the verification of
 * incoming requests and facilitates the secure transmission of data between applications.
 * </p>
 * * <h3>Key Features:</h3>
 * <ul>
 * <li><b>Identity Verification:</b> Validates calling apps using package names or certificate pinning.</li>
 * <li><b>UI Rationale:</b> Automatically generates and displays a security dialog to the user.</li>
 * <li><b>Lifecycle Awareness:</b> Manages result codes and intent extras for both Activities and Fragments.</li>
 * <li><b>Flexibility:</b> Supports custom layouts, themes, and specialized access managers.</li>
 * </ul>
 *
 * @since 1.0
 * @author David Onyia
 */
public final class IntentGuardManager {

    private static final String DEBUG_NAME
            = IntentGuardManager.class.getSimpleName();

    private Fragment mFragment;
    private Activity mActivity;
    private PackageInfo packageInfo;

    private final OnClickHelper mCancelButtonClickHelper;
    private final OnClickHelper mApproveButtonClickHelper;
    private final IntentProcessor mIntentProcessor;
    private final AuthDialogBuilder mAuthDialogBuilder;
    private final AccessManager mAccessManager;

    @Nullable
    private DialogInfo dialogInfo;

    @Nullable
    private ResultListener mResultListener;

    @Nullable
    private RequestListener mRequestListener;

    /**
     * Constructs a manager with an Activity context using the default library layout.
     *
     * @param mContext       The Activity context.
     * @param mAccessManager Custom access manager or {@code null} to use the default implementation.
     */
    public IntentGuardManager(@NonNull Context mContext,
                              @Nullable AccessManager mAccessManager){
        this(mContext,mAccessManager,R.layout.auth_bottom_dialog_layout);
    }

    /**
     * Constructs a manager with an Activity context and a custom layout.
     *
     * @param mContext          The Activity context.
     * @param mAccessManager    Custom access manager.
     * @param mAuthDialogLayout Layout resource ID for the authentication dialog.
     */
    public IntentGuardManager(@NonNull Context mContext,
                              @Nullable AccessManager mAccessManager,
                              @LayoutRes int mAuthDialogLayout){

        this(mContext,mAccessManager,mAuthDialogLayout,DialogType.DEFAULT);
    }

    /**
     * Constructs a manager with an Activity context, custom layout, and dialog type.
     *
     * @param mContext          The Activity context.
     * @param mAccessManager    Custom access manager.
     * @param mAuthDialogLayout Layout resource ID.
     * @param dialogType        The behavioral type of the dialog.
     */
    public IntentGuardManager(@NonNull Context mContext,
                              @Nullable AccessManager mAccessManager,
                              @LayoutRes int mAuthDialogLayout,
                              @Nullable DialogType dialogType){

        this(mContext,mAccessManager,false,mAuthDialogLayout,AuthDialogBuilder.NO_THEME,dialogType);
    }

    /**
     * Constructs a manager with a Fragment context using the default library layout.
     *
     * @param mFragment      The hosting Fragment.
     * @param mAccessManager Custom access manager.
     */
    public IntentGuardManager(@NonNull Fragment mFragment,
                              @Nullable AccessManager mAccessManager){

        this(mFragment,mAccessManager,R.layout.auth_bottom_dialog_layout);
    }

    /**
     * Constructs a manager with a Fragment context and a custom layout.
     *
     * @param mFragment         The hosting Fragment.
     * @param mAccessManager    Custom access manager.
     * @param mAuthDialogLayout Layout resource ID.
     */
    public IntentGuardManager(@NonNull Fragment mFragment,
                              @Nullable AccessManager mAccessManager,
                              @LayoutRes int mAuthDialogLayout){

        this(mFragment,mAccessManager,mAuthDialogLayout,DialogType.DEFAULT);
    }

    /**
     * Constructs a manager with a Fragment context, custom layout, and dialog type.
     *
     * @param mFragment         The hosting Fragment.
     * @param mAccessManager    Custom access manager.
     * @param mAuthDialogLayout Layout resource ID.
     * @param dialogType        The behavioral type of the dialog.
     */
    public IntentGuardManager(@NonNull Fragment mFragment,
                              @Nullable AccessManager mAccessManager,
                              @LayoutRes int mAuthDialogLayout,
                              @Nullable DialogType dialogType){

        this(mFragment,mAccessManager,false,mAuthDialogLayout,AuthDialogBuilder.NO_THEME,dialogType);
    }

    /**
     * Fully configurable constructor for Fragment usage.
     *
     * @param mFragment         The hosting Fragment.
     * @param mAccessManager    Custom access manager logic.
     * @param canDismiss        Whether the dialog can be dismissed by tapping outside.
     * @param mAuthDialogLayout Layout resource ID.
     * @param dialogTheme       Style resource for the dialog.
     * @param dialogType        Behavioral type of the dialog.
     */
    public IntentGuardManager(@NonNull Fragment mFragment,
                              @Nullable AccessManager mAccessManager,
                              boolean canDismiss,
                              @LayoutRes int mAuthDialogLayout,
                              @StyleRes int dialogTheme,
                              @Nullable DialogType dialogType){

        this.mFragment = mFragment;
        this.mAuthDialogBuilder = new AuthDialogBuilder(mFragment.getParentFragmentManager(),canDismiss,mAuthDialogLayout,dialogTheme,dialogType);
        this.mAccessManager = mAccessManager == null ? new DefaultAccessManager() : mAccessManager;
        this.mApproveButtonClickHelper = new OnClickHelper(OnClickHelper.APPROVE_BUTTON,mFragment,this.mAccessManager,mAuthDialogBuilder);
        this.mCancelButtonClickHelper = new OnClickHelper(OnClickHelper.CANCEL_BUTTON,mFragment,this.mAccessManager,mAuthDialogBuilder);
        this.mIntentProcessor = new IntentProcessor(mFragment,this.mAccessManager);

        this.mAccessManager.setApplicationContext(mFragment.requireContext());
    }

    /**
     * Fully configurable constructor for Activity usage.
     *
     * @param mContext          The Activity context.
     * @param mAccessManager    Custom access manager logic.
     * @param canDismiss        Whether the dialog can be dismissed by tapping outside.
     * @param mAuthDialogLayout Layout resource ID.
     * @param dialogTheme       Style resource for the dialog.
     * @param dialogType        Behavioral type of the dialog.
     */
    public IntentGuardManager(@NonNull Context mContext,
                              @Nullable AccessManager mAccessManager,
                              boolean canDismiss,
                              @LayoutRes int mAuthDialogLayout,
                              @StyleRes int dialogTheme,
                              @Nullable DialogType dialogType){

        this.mActivity = Utils.ensureIsActivityDescendant(mContext,
                mContext.getClass().getSimpleName()
                        + " cannot be used in IntentGuardManager use an activity instead.");
        this.mAuthDialogBuilder = new AuthDialogBuilder(mContext,canDismiss,mAuthDialogLayout,dialogTheme,dialogType);
        this.mAccessManager = mAccessManager == null ? new DefaultAccessManager() : mAccessManager;
        this.mApproveButtonClickHelper = new OnClickHelper(OnClickHelper.APPROVE_BUTTON,mContext,this.mAccessManager,mAuthDialogBuilder);
        this.mCancelButtonClickHelper = new OnClickHelper(OnClickHelper.CANCEL_BUTTON,mContext,this.mAccessManager,mAuthDialogBuilder);
        this.mIntentProcessor = new IntentProcessor(mActivity,this.mAccessManager);

        this.mAccessManager.setApplicationContext(mActivity);
    }

    /**
     * Sets a custom theme for the authentication dialog.
     *
     * @param dialogTheme The style resource ID.
     * @return The current manager instance.
     */
    public IntentGuardManager setTheme(@StyleRes int dialogTheme){
        mAuthDialogBuilder.setTheme(dialogTheme);
        return this;
    }

    /**
     * Links a custom View ID to the cancel action.
     *
     * @param resId The resource ID of the cancel button.
     * @return The current manager instance.
     */
    public IntentGuardManager registerCancelButton(@IdRes int resId){
        mAuthDialogBuilder.registerCancelButton(resId,mCancelButtonClickHelper);
        return this;
    }

    /**
     * Links a custom View ID to the approve action.
     *
     * @param resId The resource ID of the approve button.
     * @return The current manager instance.
     */
    public IntentGuardManager registerApproveButton(@IdRes int resId){
        mAuthDialogBuilder.registerApproveButton(resId,mApproveButtonClickHelper);
        return this;
    }

    /**
     * Configures whether the library should strictly enforce the trusted apps list.
     *
     * @param enforce {@code true} to block untrusted apps; {@code false} to allow with warning.
     * @return The current manager instance.
     */
    public IntentGuardManager enforceTrustedAppOnly(boolean enforce){
        mAccessManager.setEnableTrustedApp(enforce);
        return this;
    }

    /**
     * Toggles secure request mode which enables/disables session code generation.
     *
     * @param secure {@code true} to enable code generation.
     * @return The current manager instance.
     */
    public IntentGuardManager secureRequest(boolean secure){
        mAccessManager.setEnableCodeGeneration(secure);
        return this;
    }

    /**
     * Adds an app to the trusted list based solely on its package name.
     *
     * @param packageName The package name to trust.
     * @return The current manager instance.
     */
    public IntentGuardManager addTrustedApp(@NonNull String packageName){
        mAccessManager.addTrustApp(packageName,null,false);
        return this;
    }

    /**
     * Adds an app to the trusted list with a package name and its SHA-256 certificate hash.
     *
     * @param packageName The package name to trust.
     * @param sha256Cert  The SHA-256 signature hash of the app.
     * @return The current manager instance.
     */
    public IntentGuardManager addTrustAppWithCert(@NonNull String packageName,@NonNull String sha256Cert){
        mAccessManager.addTrustApp(packageName,sha256Cert,true);
        return this;
    }

    /**
     * Removes an app from the trusted list.
     *
     * @param packageName The package name to remove.
     * @return The current manager instance.
     */
    public IntentGuardManager removeTrustedApp(String packageName){
        mAccessManager.removeTrustApp(packageName);
        return this;
    }

    /**
     * Sets the custom text and icons to be displayed on the rationale dialog.
     *
     * @param dialogInfo The dialog metadata container.
     * @return The current manager instance.
     */
    public IntentGuardManager setDialogInfo(@Nullable DialogInfo dialogInfo) {
        this.dialogInfo = dialogInfo;
        return this;
    }

    /**
     * Attaches a listener to receive callbacks regarding the final result of the auth process.
     *
     * @param resultListener The result listener callback.
     * @return The current manager instance.
     */
    public IntentGuardManager setResultListener(@Nullable ResultListener resultListener) {
        this.mResultListener = resultListener;
        this.mIntentProcessor.setResultListener(resultListener);

        //Check if the default manager is in use
        if (this.mAccessManager instanceof DefaultAccessManager){
            ((DefaultAccessManager) this.mAccessManager).setResultListener(mResultListener);
        }

        return this;
    }


    /**
     * Attaches a listener to receive the raw intent when the request is validated.
     *
     * @param requestListener The request listener callback.
     * @return The current manager instance.
     */
    public IntentGuardManager setRequestListener(@Nullable RequestListener requestListener) {
        this.mRequestListener = requestListener;
        mCancelButtonClickHelper.setRequestListener(mRequestListener);
        mApproveButtonClickHelper.setRequestListener(mRequestListener);
        return this;
    }

    /**
     * Sets the data bundle to be sent as a response to the requester.
     *
     * @param responseBody The response payload.
     * @return The current manager instance.
     */
    public IntentGuardManager setResponse(@NonNull Bundle responseBody){
        mIntentProcessor.setResponse(responseBody);
        return this;
    }

    /**
     * Configures the request code used for Activity result delivery.
     *
     * @param requestCode The integer request code.
     * @return The current manager instance.
     */
    public IntentGuardManager setRequestCode(int requestCode){
        mIntentProcessor.setRequestCode(requestCode);
        return this;
    }

    /**
     * Must be called in {@code onActivityResult} to process results returned from a request.
     *
     * @param requestCode The request code from the activity lifecycle.
     * @param resultCode  The result code from the activity lifecycle.
     * @param data        The intent data returned.
     */
    public void registerActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        mIntentProcessor.registerActivityResult(requestCode, resultCode, data);
    }

    /**
     * Starts listening for an incoming request. The dialog will not be cancelable.
     */
    public void awaitRequest(){
        awaitRequest(false);
    }

    /**
     * Starts listening for an incoming request with a configurable cancellability.
     *
     * @param cancelAble {@code true} if the user can cancel the dialog by clicking outside.
     */
    public void awaitRequest(boolean cancelAble){

        mAuthDialogBuilder.setCancelAble(cancelAble);

        if (isCalledFromActivity()){
            handleRequestCalledFromActivity();
        }else {
            handleRequestCalledFromFragment();
        }
    }

    /**
     * Returns the currently set response bundle.
     *
     * @return The response payload, or {@code null}.
     */
    @Nullable
    public Bundle getResponse(){
        return mIntentProcessor.getResponse();
    }

    /**
     * Returns the error message generated during the last access check.
     *
     * @return A string description of the error.
     */
    @Nullable
    public String getErrorMessage(){
        return mAccessManager.getErrorMessage();
    }

    /**
     * Returns the error code generated during the last access check.
     *
     * @return The integer error code.
     */
    public int getErrorCode(){
        return mAccessManager.getErrorCode();
    }

    /**
     * Processes requests initiated from an Activity. Handles package extraction,
     * trust verification, and UI population.
     */
    private void handleRequestCalledFromActivity(){
        Intent requestIntent = mActivity.getIntent();

        mAccessManager.setIncomingIntent(requestIntent);

        if (Utils.isRequestSecure(requestIntent)){

            ComponentName requesterComponentName
                    = mActivity.getCallingActivity();

            String requesterPackageName = requesterComponentName != null
                    ? requesterComponentName.getPackageName() : null;

            PackageManager packageManager
                    = mActivity.getPackageManager();

            if (requesterPackageName == null){
                notifyResultListenerOfError("Requester package name not found.");
                return;
            }

            try {
                packageInfo = packageManager.getPackageInfo(requesterPackageName,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES);

            } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                notifyResultListenerOfError(nameNotFoundException.getMessage() != null ? nameNotFoundException.getMessage()
                        : "Package or component name not found.");
                return;
            }

            CharSequence requesterAppName
                    = packageInfo.applicationInfo.loadLabel(packageManager);

            Drawable requesterAppIcon
                    = packageInfo.applicationInfo.loadIcon(packageManager);

            //Check if the default layout is in use then
            //we set the data to be displayed.
            if (mAuthDialogBuilder.isDefaultLayoutInUse()){

                TextView headerText = mAuthDialogBuilder.findViewById(R.id.m_headerText);
                ImageView requesterAppImage = mAuthDialogBuilder.findViewById(R.id.m_requester_app_icon);
                TextView descriptionText = mAuthDialogBuilder.findViewById(R.id.m_descriptionText);
                RecyclerView requestPermissionList = mAuthDialogBuilder.findViewById(R.id.m_permissionList);
                Group mPermissionAvailable = mAuthDialogBuilder.findViewById(R.id.m_permissionAvailable);
                Button mAcceptButton = mAuthDialogBuilder.findViewById(R.id.m_approveButton);
                Button mCancelButton = mAuthDialogBuilder.findViewById(R.id.m_cancelButton);

                boolean allNonNull = !Utils.isNull(headerText) && !Utils.isNull(requesterAppImage)
                        && !Utils.isNull(descriptionText) && !Utils.isNull(requestPermissionList)
                        && !Utils.isNull(mAcceptButton) && !Utils.isNull(mCancelButton)
                        && !Utils.isNull(mPermissionAvailable);

                String headerString = Utils.namePlacement(mActivity,
                        R.string.m_requester_name_available_request_header,
                        (String) requesterAppName);

                if (dialogInfo != null){

                    if (mAccessManager.isTrustedAppEnabled()){

                        if (allNonNull){

                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            if (mAccessManager.isCallerTrusted(requesterPackageName)){
                                //Set the defined DialogInfo
                                headerText.setText(dialogInfo.getHeaderText());
                                descriptionText.setText(dialogInfo.getBodyText());
                                mCancelButton.setText(dialogInfo.getCancelButtonText());
                                mAcceptButton.setText(dialogInfo.getAcceptButtonText());
                            }else {

                                String descriptionString = Utils.namePlacement(mActivity,
                                        R.string.m_untrusted_requester_name_available_requesting_in_secure_mode_description,
                                        requesterPackageName);

                                headerText.setText(headerString);
                                descriptionText.setText(descriptionString);
                                mAcceptButton.setText(R.string.m_approve_untrusted_text);
                            }

                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }else {
                        if (allNonNull){
                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            //Set the defined DialogInfo
                            headerText.setText(dialogInfo.getHeaderText());
                            descriptionText.setText(dialogInfo.getBodyText());
                            mCancelButton.setText(dialogInfo.getCancelButtonText());
                            mAcceptButton.setText(dialogInfo.getAcceptButtonText());
                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }
                }else {

                    if (mAccessManager.isTrustedAppEnabled()){

                        if (allNonNull){

                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            if (mAccessManager.isCallerTrusted(requesterPackageName)){

                                String descriptionString = Utils.namePlacement(mActivity,
                                        R.string.m_trusted_requester_name_available_requesting_description,
                                        (String) requesterAppName);

                                headerText.setText(headerString);
                                descriptionText.setText(descriptionString);
                            }else {

                                String descriptionString = Utils.namePlacement(mActivity,
                                        R.string.m_untrusted_requester_name_available_requesting_in_secure_mode_description,
                                        (String) requesterAppName);

                                headerText.setText(headerString);
                                descriptionText.setText(descriptionString);
                                mAcceptButton.setText(R.string.m_approve_untrusted_text);
                            }

                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }else {

                        if (allNonNull){
                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            String descriptionString = Utils.namePlacement(mActivity,
                                    R.string.m_trusted_requester_name_available_requesting_description,
                                    (String) requesterAppName);

                            headerText.setText(headerString);
                            descriptionText.setText(descriptionString);
                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }

                }

                //Defensive check
                mCancelButton.setOnClickListener(mCancelButtonClickHelper);
                mAcceptButton.setOnClickListener(mApproveButtonClickHelper);

                parsePermissions(requestIntent, requestPermissionList, mPermissionAvailable);

            }else {
                if (!mAuthDialogBuilder.areAllButtonsRegistered()){
                    throw new RuntimeException("Using custom layout requires you to register both (acceptButton and cancelButton).");
                }

            }

            Log.d(DEBUG_NAME, "Incoming secure intent request - show auth dialog");

            //Show the auth dialog.
            if (!mAuthDialogBuilder.isShowing()){
                mAuthDialogBuilder.showAuthDialog();
            }

        } else if (Utils.isRequestDefault(requestIntent)) {
            Log.d(DEBUG_NAME, "Incoming intent request with default body — bypassing dialog passing raw intent");
            notifyRequestListener(requestIntent,DEFAULT_TYPE);
        }else {
            Log.d(DEBUG_NAME, "Incoming intent request without token/body and default body — bypassing dialog passing raw intent");
            notifyRequestListener(requestIntent,UNKNOWN_TYPE);
        }
    }

    /**
     * Processes requests initiated from a Fragment.
     */
    private void handleRequestCalledFromFragment(){

        Activity parentActivity
                = mFragment.requireActivity();


        Intent requestIntent = parentActivity.getIntent();

        mAccessManager.setIncomingIntent(requestIntent);

        if (Utils.isRequestSecure(requestIntent)){

            ComponentName requesterComponentName
                    = parentActivity.getCallingActivity();

            String requesterPackageName = requesterComponentName != null
                    ? requesterComponentName.getPackageName() : null;

            PackageManager packageManager
                    = parentActivity.getPackageManager();

            if (requesterPackageName == null){
                notifyResultListenerOfError("Requester package name not found.");
                return;
            }

            try {
                packageInfo = packageManager.getPackageInfo(requesterPackageName,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES);

            } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                notifyResultListenerOfError(nameNotFoundException.getMessage() != null ? nameNotFoundException.getMessage()
                        : "Package or component name not found.");
                return;
            }

            CharSequence requesterAppName
                    = packageInfo.applicationInfo.loadLabel(packageManager);

            Drawable requesterAppIcon
                    = packageInfo.applicationInfo.loadIcon(packageManager);

            //Check if the default layout is in use then
            //we set the data to be displayed.
            if (mAuthDialogBuilder.isDefaultLayoutInUse()){

                TextView headerText = mAuthDialogBuilder.findViewById(R.id.m_headerText);
                ImageView requesterAppImage = mAuthDialogBuilder.findViewById(R.id.m_requester_app_icon);
                TextView descriptionText = mAuthDialogBuilder.findViewById(R.id.m_descriptionText);
                RecyclerView requestPermissionList = mAuthDialogBuilder.findViewById(R.id.m_permissionList);
                Group mPermissionAvailable = mAuthDialogBuilder.findViewById(R.id.m_permissionAvailable);
                Button mAcceptButton = mAuthDialogBuilder.findViewById(R.id.m_approveButton);
                Button mCancelButton = mAuthDialogBuilder.findViewById(R.id.m_cancelButton);

                boolean allNonNull = !Utils.isNull(headerText) && !Utils.isNull(requesterAppImage)
                        && !Utils.isNull(descriptionText) && !Utils.isNull(requestPermissionList)
                        && !Utils.isNull(mAcceptButton) && !Utils.isNull(mCancelButton)
                        && !Utils.isNull(mPermissionAvailable);

                String headerString = Utils.namePlacement(parentActivity,
                        R.string.m_requester_name_available_request_header,
                        (String) requesterAppName);

                if (dialogInfo != null){

                    if (mAccessManager.isTrustedAppEnabled()){

                        if (allNonNull){

                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            if (mAccessManager.isCallerTrusted(requesterPackageName)){
                                //Set the defined DialogInfo
                                headerText.setText(dialogInfo.getHeaderText());
                                descriptionText.setText(dialogInfo.getBodyText());
                                mCancelButton.setText(dialogInfo.getCancelButtonText());
                                mAcceptButton.setText(dialogInfo.getAcceptButtonText());
                            }else {

                                String descriptionString = Utils.namePlacement(parentActivity,
                                        R.string.m_untrusted_requester_name_available_requesting_in_secure_mode_description,
                                        (String) requesterAppName);

                                headerText.setText(headerString);
                                descriptionText.setText(descriptionString);
                                mAcceptButton.setText(R.string.m_approve_untrusted_text);
                            }

                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }else {
                        if (allNonNull){
                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            //Set the defined DialogInfo
                            headerText.setText(dialogInfo.getHeaderText());
                            descriptionText.setText(dialogInfo.getBodyText());
                            mCancelButton.setText(dialogInfo.getCancelButtonText());
                            mAcceptButton.setText(dialogInfo.getAcceptButtonText());
                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }
                }else {

                    if (mAccessManager.isTrustedAppEnabled()){

                        if (allNonNull){

                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            if (mAccessManager.isCallerTrusted(requesterPackageName)){

                                String descriptionString = Utils.namePlacement(parentActivity,
                                        R.string.m_trusted_requester_name_available_requesting_description,
                                        (String) requesterAppName);

                                headerText.setText(headerString);
                                descriptionText.setText(descriptionString);
                            }else {

                                String descriptionString = Utils.namePlacement(parentActivity,
                                        R.string.m_untrusted_requester_name_available_requesting_in_secure_mode_description,
                                        (String) requesterAppName);

                                headerText.setText(headerString);
                                descriptionText.setText(descriptionString);
                                mAcceptButton.setText(R.string.m_approve_untrusted_text);
                            }

                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }else {

                        if (allNonNull){
                            requesterAppImage.setImageDrawable(requesterAppIcon);

                            String descriptionString = Utils.namePlacement(parentActivity,
                                    R.string.m_trusted_requester_name_available_requesting_description,
                                    (String) requesterAppName);

                            headerText.setText(headerString);
                            descriptionText.setText(descriptionString);
                        }else {
                            notifyResultListenerOfError("Error finding default view. Please use a custom layout.");
                            return;
                        }
                    }

                }

                //Defensive check
                mCancelButton.setOnClickListener(mCancelButtonClickHelper);
                mAcceptButton.setOnClickListener(mApproveButtonClickHelper);

                parsePermissions(requestIntent, requestPermissionList, mPermissionAvailable);

            }else {
                if (!mAuthDialogBuilder.areAllButtonsRegistered()){
                    throw new RuntimeException("Using custom layout requires you to register both (acceptButton and cancelButton).");
                }
            }

            Log.d(DEBUG_NAME, "Incoming secure intent request - show auth dialog");

            //Show the auth dialog.
            if (!mAuthDialogBuilder.isShowing()){
                mAuthDialogBuilder.showAuthDialog();
            }

        } else if (Utils.isRequestDefault(requestIntent)) {
            Log.d(DEBUG_NAME, "Incoming intent request with default body — bypassing dialog passing raw intent");
            notifyRequestListener(requestIntent,DEFAULT_TYPE);
        }else {
            Log.d(DEBUG_NAME, "Incoming intent request without token/body and default body — bypassing dialog passing raw intent");
            notifyRequestListener(requestIntent,UNKNOWN_TYPE);
        }

    }

    /**
     * Extracts permission keys from the intent and updates the UI synchronously.
     * <p>
     * This eliminates race conditions by ensuring the Adapter is attached
     * before the Dialog's measure pass occurs.
     * </p>
     */
    private void parsePermissions(@NonNull Intent requestIntent,
                                  RecyclerView requestPermissionList,
                                  Group mPermissionAvailable) {

        // 1. Get raw keys from Intent
        List<String> requestPermissions = requestIntent.getStringArrayListExtra(Metadata.REQUEST_PERMISSIONS.getKey());

        if (requestPermissions != null && !requestPermissions.isEmpty()) {
            List<PermissionInfo> permissionInfos = new java.util.ArrayList<>();
            com.intent.guard.request.RequestPermission registry = com.intent.guard.request.RequestPermission.getInstance();

            // 2. Map strings to PermissionInfo objects immediately
            for (String permission : requestPermissions) {
                PermissionInfo info = registry.getDefinePermissionInfo(permission);
                if (info != null) {
                    permissionInfos.add(info);
                }
            }

            // 3. Bind to UI
            if (!permissionInfos.isEmpty()) {
                PermissionAdapter adapter = new PermissionAdapter(permissionInfos);
                requestPermissionList.setAdapter(adapter);
                mPermissionAvailable.setVisibility(View.VISIBLE);

                // Notify data set change to force immediate layout calculation
                adapter.notifyDataSetChanged();
            } else {
                mPermissionAvailable.setVisibility(View.GONE);
            }
        } else {
            mPermissionAvailable.setVisibility(View.GONE);
        }
    }

    /**
     * Executes the delivery of the set response bundle to the requester.
     */
    public void sendResponse(){
        mIntentProcessor.sendResponse();
    }

    /**
     * Sends a request to another application.
     *
     * @param request The intent request container.
     */
    public void sendRequest(@NonNull IntentRequest request){
        mIntentProcessor.sendRequest(request);
    }

    /**
     * Sends a request with a specific request code.
     *
     * @param request     The intent request container.
     * @param requestCode The unique request code.
     */
    public void sendRequestWithCode(@NonNull IntentRequest request,int requestCode){
        mIntentProcessor.sendRequest(request, requestCode);
    }

    /**
     * Notifies the result listener when an internal error occurs.
     */
    private void notifyResultListenerOfError(String errorMessage){

        //Notifies the application about internal error.
        //then we end the process here.
        if (mResultListener != null){
            mResultListener.onCancelled(AuthException.INTERNAL_ERROR);
        }

        Log.e(DEBUG_NAME,errorMessage);
    }

    /**
     * Dispatches the incoming intent to the request listener.
     */
    private void notifyRequestListener(Intent requestIntent,int intentType){
        if (mRequestListener != null) mRequestListener.onRequestReceived(requestIntent,intentType);
    }

    /**
     * @return {@code true} if the manager was initialized with an Activity context.
     */
    private boolean isCalledFromActivity(){
        return mFragment == null && mActivity != null;
    }
}