package com.intent.guard.core.internal;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.intent.guard.ResultListener;
import com.intent.guard.auth.AuthException;
import com.intent.guard.core.Metadata;
import com.intent.guard.core.access.AccessManager;
import com.intent.guard.request.IntentRequest;

/**
 * The internal engine responsible for handling the lifecycle of an Intent-based request/response exchange.
 * <p>
 * IntentProcessor manages the low-level mechanics of dispatching secure Intents via
 * {@link ActivityCompat#startActivityForResult}, generating session tokens through the
 * {@link AccessManager}, and intercepting the results to validate security integrity.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class IntentProcessor {

    private static final String DEBUG_NAME = IntentProcessor.class.getSimpleName();
    private static final int DEFAULT_REQUEST_CODE = 19022;
    private int requestCode;
    private final Fragment mFragment;
    private final Activity mActivity;
    private final AccessManager mAccessManager;

    @Nullable
    private ResultListener mResultListener;

    /**
     * Constructs a processor for Fragment-based flows.
     * @param mFragment      The parent Fragment.
     * @param mAccessManager The security manager used for token operations.
     */
    public IntentProcessor(@NonNull Fragment mFragment, @NonNull AccessManager mAccessManager){
        this(null, mFragment, mAccessManager, DEFAULT_REQUEST_CODE);
    }

    /**
     * Constructs a processor for Activity-based flows.
     * @param mActivity      The host Activity.
     * @param mAccessManager The security manager used for token operations.
     */
    public IntentProcessor(@NonNull Activity mActivity, @NonNull AccessManager mAccessManager){
        this(mActivity, null, mAccessManager, DEFAULT_REQUEST_CODE);
    }

    private IntentProcessor(@Nullable Activity mActivity, @Nullable Fragment mFragment, @NonNull AccessManager mAccessManager, int requestCode){
        this.mActivity = mActivity;
        this.mFragment = mFragment;
        this.mAccessManager = mAccessManager;
        this.requestCode = requestCode;
    }

    /**
     * Sets the listener to receive final results (success or cancellation).
     * @param mResultListener The callback listener.
     */
    public void setResultListener(@Nullable ResultListener mResultListener) {
        this.mResultListener = mResultListener;
    }

    /**
     * Sets a custom request code for the startActivityForResult call.
     * @param requestCode The integer request code.
     */
    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    /**
     * Sets the payload to be returned in the response Intent.
     * @param mResponse The response bundle.
     */
    public void setResponse(@NonNull Bundle mResponse){
        mAccessManager.setResponseBody(mResponse);
    }

    /**
     * Dispatches an outgoing request using the default request code.
     * @param intentRequest The model containing the target and payload.
     */
    public void sendRequest(@NonNull IntentRequest intentRequest){
        sendRequest(intentRequest, requestCode);
    }

    /**
     * Packages the current response payload and security tokens into an Intent
     * and returns it to the calling application via setResult.
     * <p>This method automatically finishes the host Activity.</p>
     */
    public void sendResponse(){
        Intent incomingIntent = mAccessManager.getIncomingIntent();

        String requestToken = incomingIntent.hasExtra(Metadata.REQUEST_TOKEN.getKey())
                ? incomingIntent.getStringExtra(Metadata.REQUEST_TOKEN.getKey()) : null;

        Intent responseIntent = new Intent();
        Bundle responseBody = mAccessManager.getResponseBody();

        if (requestToken != null){
            Log.d(DEBUG_NAME, "Token present — sending authenticated response");
            responseIntent.putExtra(Metadata.REQUEST_TOKEN.getKey(), requestToken);
            responseIntent.putExtra(Metadata.RESPONSE_BODY.getKey(), responseBody);
        } else {
            Log.d(DEBUG_NAME, "No token present — sending default response");
            responseIntent.putExtra(Metadata.DEFAULT_RESPONSE_BODY.getKey(), responseBody);
        }

        if (isCalledFromActivity()){
            mActivity.setResult(RESULT_OK, responseIntent);
            mActivity.finish();
        } else if (mFragment != null) {
            Activity mParentActivity = mFragment.getActivity();
            if (mParentActivity != null){
                mParentActivity.setResult(RESULT_OK, responseIntent);
                mParentActivity.finish();
            }
        }
    }

    /**
     * Initiates a request to another application. If security is enabled,
     * a session token is generated and injected into the Intent extras.
     *
     * @param intentRequest The model containing the intent data.
     * @param requestCode   A custom code to track this specific request.
     */
    public void sendRequest(@NonNull IntentRequest intentRequest, int requestCode){
        this.requestCode = requestCode;
        Intent requestIntent = intentRequest.getIntent();

        if (!requestIntent.hasExtra(Metadata.REQUEST_BODY.getKey())) {
            requestIntent.putExtra(Metadata.REQUEST_BODY.getKey(), new Bundle());
        }

        if (!mAccessManager.isEnableCodeGeneration()){
            dispatchIntent(requestIntent);
            return;
        }

        mAccessManager.generateAccessToken();
        requestIntent.putExtra(Metadata.REQUEST_TOKEN.getKey(), mAccessManager.getSessionToken());
        dispatchIntent(requestIntent);
    }

    private void dispatchIntent(Intent requestIntent) {
        if (isCalledFromActivity()){
            ActivityCompat.startActivityForResult(mActivity, requestIntent, requestCode, requestIntent.getExtras());
        } else if (mFragment != null) {
            Activity mParentActivity = mFragment.getActivity();
            if (mParentActivity != null){
                ActivityCompat.startActivityForResult(mParentActivity, requestIntent, requestCode, requestIntent.getExtras());
            }
        }
    }

    /**
     * Processes the data returned from an external application.
     * <p>
     * This method handles three scenarios:
     * 1. Cancellation: Notifies listener of user cancellation.
     * 2. Default Response: Extracts unauthenticated data if no tokens are present.
     * 3. Secure Response: Extracts tokens and payloads, then triggers validation via {@link AccessManager}.
     * </p>
     *
     * @param requestCode The request code from the lifecycle callback.
     * @param resultCode  The result code (OK or CANCELED).
     * @param data        The Intent returned by the target application.
     */
    public void registerActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == this.requestCode && resultCode == RESULT_OK){
            String sessionCode = data != null && data.hasExtra(Metadata.REQUEST_TOKEN.getKey())
                    ? data.getStringExtra(Metadata.REQUEST_TOKEN.getKey()) : null;

            if (sessionCode == null){
                Bundle defaultResponse = data != null && data.hasExtra(Metadata.DEFAULT_RESPONSE_BODY.getKey())
                        ?  data.getBundleExtra(Metadata.DEFAULT_RESPONSE_BODY.getKey())
                        : data != null ? data.getExtras() : null;

                if (mResultListener != null) mResultListener.onResultReceived(defaultResponse);
                return;
            }

            Bundle secureResponse = data.hasExtra(Metadata.RESPONSE_BODY.getKey())
                    ? data.getBundleExtra(Metadata.RESPONSE_BODY.getKey()) : null;

            mAccessManager.setResultBody(secureResponse);

            if (!mAccessManager.isEnableCodeGeneration()){
                if (mResultListener != null) mResultListener.onResultReceived(mAccessManager.getResultBody());
                return;
            }

            mAccessManager.validateToken(sessionCode);
        } else if (requestCode == this.requestCode && resultCode == RESULT_CANCELED) {
            if (mResultListener != null) mResultListener.onCancelled(AuthException.USER_CANCELED);
        }
    }

    /**
     * @return The current response payload bundle stored in the security manager.
     */
    @Nullable
    public Bundle getResponse(){
        return mAccessManager.getResponseBody();
    }

    private boolean isCalledFromActivity(){
        return mFragment == null && mActivity != null;
    }
}