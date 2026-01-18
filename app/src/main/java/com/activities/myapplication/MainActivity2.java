package com.activities.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.button.MaterialButton;
import com.intent.guard.IntentGuardManager;
import com.intent.guard.RequestListener;
import com.intent.guard.core.Metadata;
import com.intent.guard.permission.PermissionInfo;
import com.intent.guard.request.RequestPermission;

/**
 * The "Provider" activity that receives and processes secure requests from other apps.
 * <p>
 * This class demonstrates the server-side logic of the IntentGuard library. It validates
 * the caller's identity, displays the security rationale dialog to the user, and processes
 * the decrypted payload upon user approval.
 * </p>
 *
 * <h3>Provider Workflow:</h3>
 * <ol>
 * <li>Initializes {@link IntentGuardManager} and defines trust policies (e.g., allowlist).</li>
 * <li>Registers custom permission metadata via {@link RequestPermission} to show in the UI.</li>
 * <li>Calls {@link IntentGuardManager#awaitRequest()} to trigger the security handshake.</li>
 * <li>Handles the verified data in {@link #onRequestReceived(Intent, int)}.</li>
 * <li>Sends a secure response back to the requester via {@link IntentGuardManager#sendResponse()}.</li>
 * </ol>
 *
 *
 *
 * @author David Onyia
 * @version 1.0
 */
public class MainActivity2 extends AppCompatActivity implements RequestListener {

    /**
     * The facade component coordinating security checks and dialog generation.
     */
    private IntentGuardManager intentGuardManager;

    private MaterialButton sendReponse;
    private TextView requestText;

    /**
     * Initializes the provider activity and sets up the security environment.
     * <p>
     * This method configures the manager to enforce trusted apps only and adds the
     * current package to the trust list. It also pre-defines what specific permission
     * strings (IDs) mean visually to the user.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        requestText =  findViewById(R.id.requestText);
        sendReponse = findViewById(R.id.response);

        RequestPermission.getInstance()
                .definePermissionInfo("com.permission.userId",
                        PermissionInfo.builder()
                                .setImage(R.drawable.ic_launcher_foreground)
                                .setText("User Identity Access: Required to verify your profile information.").build())
                .definePermissionInfo("com.permission.user.account.balance",
                        new PermissionInfo(R.drawable.ic_launcher_foreground,
                                "Financial Data: Required to view account balances for transaction processing."));


        intentGuardManager = new IntentGuardManager(this, null)
                .addTrustedApp(getPackageName())
                .setRequestListener(this);

        intentGuardManager.awaitRequest();
    }

    /**
     * Callback triggered after the security handshake is successful and the user
     * has approved the request.
     * <p>
     * This method categorizes the intent into three types:
     * <ul>
     * <li>{@link #SECURE_TYPE}: Decrypted and verified payload.</li>
     * <li>{@link #DEFAULT_TYPE}: Standard intent with basic body.</li>
     * <li>{@link #UNKNOWN_TYPE}: Request missing valid security tokens.</li>
     * </ul>
     * </p>
     *
     * @param intent     The validated intent containing the requested data.
     * @param intentType The security classification of the received intent.
     */
    @Override
    public void onRequestReceived(@NonNull Intent intent, int intentType) {

        // Handling SECURE_TYPE: The data is verified and extracted from the encrypted tunnel
        if (intentType == SECURE_TYPE) {
            Bundle bundle = intent.getBundleExtra(Metadata.REQUEST_BODY.getKey());

            if (bundle != null) {

                if (bundle.containsKey("metadata")) {

                    Bundle metadata = bundle.getBundle("metadata");

                    if (metadata != null) {
                        String str = metadata.getString("data");
                        requestText.setText(String.format("Secure Request Received: %s", str));
                    }
                }
            }

            // Setup the response trigger: When the user clicks the button, send a secure
            // result back to the Requester app.
            sendReponse.setOnClickListener(view -> {
                Bundle responseBundle = new Bundle();
                responseBundle.putString("response", "Secure Response status : Success");

                intentGuardManager.setResponse(responseBundle)
                        .sendResponse();
            
            });
        }
        // Handling DEFAULT_TYPE: Fallback for non-secure standard communication
        else if (intentType == DEFAULT_TYPE) {
            Bundle bundle = intent.getBundleExtra(Metadata.REQUEST_BODY.getKey());

            if (bundle != null){
                if (bundle.containsKey("metadata")){

                    Bundle metadata = bundle.getBundle("metadata");

                    if (metadata != null) {
                        String str = metadata.getString("data");
                        requestText.setText(String.format("Default Data Received: %s", str));
                    }

                }
            }

            // Setup the response trigger: When the user clicks the button, send a secure
            // result back to the Requester app.
            sendReponse.setOnClickListener(view -> {
                Bundle responseBundle = new Bundle();
                responseBundle.putString("response", "Default Response status : Success");

                Log.d(this.getClass().getSimpleName(),"Default Response status : Success");

                intentGuardManager.setResponse(responseBundle)
                        .sendResponse();
            });
        }
        // Handling UNKNOWN_TYPE: Security violation or missing metadata
        else if (intentType == UNKNOWN_TYPE) {
            Toast.makeText(MainActivity2.this, "Request body not found or unverified", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Required implementation for menu management; not utilized in this security workflow.
     */
    @Override
    public void addMenuProvider(@NonNull MenuProvider provider, @NonNull LifecycleOwner owner, @NonNull Lifecycle.State state) {
        // No-op
    }
}