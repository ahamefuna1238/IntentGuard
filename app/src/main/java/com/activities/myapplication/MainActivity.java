package com.activities.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.intent.guard.IntentGuardManager;
import com.intent.guard.ResultListener;
import com.intent.guard.auth.AuthException;
import com.intent.guard.core.Metadata;
import com.intent.guard.request.IntentRequest;

import java.util.ArrayList;

/**
 * The entry point activity demonstrating the implementation of the IntentGuard library.
 * <p>
 * This class serves as a "Requester" application, initiating secure cross-app or
 * cross-activity communication. It implements {@link ResultListener} to handle
 * the asynchronous feedback loop of the security handshake.
 * </p>
 *
 * <h3>Security Workflow:</h3>
 * <ol>
 * <li>Initializes {@link IntentGuardManager} with an Activity context.</li>
 * <li>Configures secure request protocols (Token generation/verification).</li>
 * <li>Builds an {@link IntentRequest} containing sensitive metadata and permission claims.</li>
 * <li>Registers lifecycle results via {@code onActivityResult}.</li>
 * </ol>
 *
 *
 *
 * @author David Onyia
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements ResultListener {

    /**
     * The facade component that orchestrates all security and UI logic for IntentGuard.
     */
    private IntentGuardManager intentGuardManager;

    private TextView responseText;

    /**
     * Initializes the Activity, sets up the UI, and configures the IntentGuard security layer.
     * <p>
     * In this phase, the manager is put into {@code secureRequest(true)} mode, ensuring
     * that session tokens are generated for the outgoing intent to prevent replay attacks.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}. <b>Note: Otherwise it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseText = findViewById(R.id.responseText);
        MaterialButton defaultRequestButton = findViewById(R.id.defaultRequest);
        MaterialButton secureRequestButton = findViewById(R.id.secureRequest);

        // Initialize the manager using the default access manager and library layout
        intentGuardManager = new IntentGuardManager(this, null);

        intentGuardManager
                .setResultListener(this);

        defaultRequestButton.setOnClickListener(view -> {
            // Define the target component (The "Provider" application)
            Intent intent = new Intent(this, MainActivity2.class);

            // Mock sensitive payload
            Bundle bundle = new Bundle();
            bundle.putString("data", "Default Request data");

            // Build the default request object
            IntentRequest intentRequest = new IntentRequest(intent)
                    .putBundle("metadata", bundle);

            // Dispatch the intent with a specific request code for identification
            intentGuardManager
                    .secureRequest(false)
                    .setRequestCode(900)
                    .sendRequest(intentRequest);
        });

        // Logic for triggering a secure outgoing request
        secureRequestButton.setOnClickListener(view -> {
            // Define the target component (The "Provider" application)
            Intent intent = new Intent(this, MainActivity2.class);

            // Mock sensitive payload
            Bundle bundle = new Bundle();
            bundle.putString("data", "Secure Request data");

            // Define the list of permissions being claimed for this session
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("com.permission.userId");
            arrayList.add("com.permission.user.account.balance");

            // Build the secure request object
            IntentRequest intentRequest = new IntentRequest(intent)
                    .putBundle("metadata", bundle)
                    .setRequestPermissions(arrayList);

            // Dispatch the intent with a specific request code for identification
            intentGuardManager
                    .secureRequest(true)
                    .setRequestCode(901)
                    .sendRequest(intentRequest);
        });
    }

    /**
     * Dispatches the raw activity result back into the IntentGuard pipeline.
     * <p>
     * This is critical for the library to intercept the response from the
     * target activity, verify the session token, and extract the encrypted data.
     * </p>
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     * allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("GuardDebug", "onActivityResult triggered. Code: " + requestCode + " Result: " + resultCode);

        if (data != null) {
            String receivedToken = data.getStringExtra(Metadata.REQUEST_TOKEN.getKey());
            Log.d("GuardDebug", data.getExtras().toString());
        }

        // Pass result to the manager for processing
        intentGuardManager.registerActivityResult(requestCode, resultCode, data);
    }

    /**
     * Callback triggered when the target application successfully processes the
     * request and returns a secure response.
     *
     * @param resultBody The bundle containing the response data returned by the provider.
     */
    @Override
    public void onResultReceived(@Nullable Bundle resultBody) {

        Log.d("Requester Result Received", String.valueOf(resultBody));

        String msg = (resultBody != null) ? resultBody.getString("response") : "null response";
        responseText.setText(msg);
    }

    /**
     * Callback triggered if the security process fails or is manually cancelled by the user.
     * <p>
     * Handles specific security failure cases like {@link AuthException#TOKEN_EXPIRED}.
     * </p>
     *
     * @param reason The integer code representing the reason for cancellation or failure.
     */
    @Override
    public void onCancelled(int reason) {
        if (reason == AuthException.TOKEN_EXPIRED) {
            responseText.setText("Expired token");
        } else {
            responseText.setText("Cancelled");
        }
    }

    /**
     * Required implementation for AppCompatActivity interface; unused in this demonstration.
     *
     * @param provider The MenuProvider to be added.
     * @param owner    The LifecycleOwner whose lifecycle determines the menu's visibility.
     * @param state    The lifecycle state where the menu should be visible.
     */
    @Override
    public void addMenuProvider(@NonNull MenuProvider provider, @NonNull LifecycleOwner owner, @NonNull Lifecycle.State state) {
        // No-op
    }
}