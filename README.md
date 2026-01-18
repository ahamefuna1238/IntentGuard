# 🛡️ IntentGuard Library

[![Android SDK](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)
[![M3](https://img.shields.io/badge/Material-3-7C4DFF?style=for-the-badge)](https://m3.material.io)
[![Release](https://jitpack.io/v/ahamefuna1238/IntentGuard.svg)](https://jitpack.io/#ahamefuna1238/IntentGuard)


`IntentGuardManager` IntentGuard is a security-first Android library designed to simplify and harden Inter-Process Communication (IPC).
By acting as a facade between raw Intents and your application logic, it automates the verification of calling applications 
via certificate fingerprints and enforces one-time session tokens to prevent replay attacks. 
Built with Material 3, it provides a seamless user experience for permission rationales and secure data handshakes.

---

## 📖 Table of Contents
1. [Installation](#-installation)
2. [Architecture Overview](#-architecture-overview)
3. [Requester Implementation (Client)](#-requester-implementation-client)
4. [Provider Implementation (Server)](#-provider-implementation-server)
5. [UI Customization](#-ui-customization)
6. [Security Best Practices](#-security-best-practices)

---


## 📦 Installation
Add the JitPack repository to your root settings.gradle.kts file:
```kotlin

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

```

Add the dependency to your module-level build.gradle.kts:
```kotlin

dependencies {
    implementation("com.github.ahamefuna1238:IntentGuard:v1.0.1")
}

```

## 🏗️ Architecture Overview

The library orchestrates multiple sub-modules to ensure a "Defense in Depth" security posture:

- **`AccessManager`**: Validates the trust status and certificate fingerprints of calling applications.
- **`AuthDialogBuilder`**: Renders the M3-compliant Bottom Sheet for user consent.
- **`IntentProcessor`**: Encapsulates/Decapsulates secure data and enforces one-time session tokens.
- **`PermissionAdapter`**: Maps internal permission strings to visual rationales in a RecyclerView.

---
## 📡 Requester Implementation (Client)
The app initiating the request must implement ResultListener and bridge the Activity result lifecycle.
This allows the library to intercept and verify returning session tokens.

````java

public class MainActivity extends AppCompatActivity implements ResultListener {
    
    private void sendSecureRequest() {
        Intent target = new Intent(this, ProviderActivity.class);
        
        ArrayList<String> perms = new ArrayList<>();
        perms.add("com.permission.userId");

        IntentRequest request = new IntentRequest(target)
                .putBundle("metadata", bundle)
                .setRequestPermissions(perms);

        intentGuardManager
                .setResultListener(this)
                .setRequestCode(9)
                .sendRequest(request);
    }

    /**
     * CRITICAL: Bridge the lifecycle to IntentGuard.
     * Intercepts and verifies the session token in the returning response.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        intentGuardManager.registerActivityResult(requestCode, resultCode, data);
    }

    @Override 
    public void onResultReceived(@Nullable Bundle resultBody) {
        // Securely handle data returned from the provider
        if (resultBody != null) {
            String response = resultBody.getString("response");
        }
    }

    @Override 
    public void onCancelled(int reason) { 
        // reason: AuthException.TOKEN_EXPIRED, USER_DENIED, etc.
    }
}
````

## 🛡️ Provider Implementation (Server)
The app providing the data must define rationales and call awaitRequest() to trigger the security UI.

```java

public class ProviderActivity extends AppCompatActivity implements RequestListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Define metadata for requested permissions (shown in the rationale dialog)
        RequestPermission.getInstance().definePermissionInfo("com.permission.userId",
            new PermissionInfo(R.mipmap.ic_launcher_round, "Allows access to your ID for profile verification."));

        intentGuardManager
                .setRequestListener(this)
                .awaitRequest(); // Intercepts intent and shows the M3 Dialog
    }

    @Override
    public void onRequestReceived(@NonNull Intent intent, int intentType) {
        if (intentType == SECURE_TYPE) {
            // Data is extracted from the Metadata.REQUEST_BODY key
            Bundle body = intent.getBundleExtra(Metadata.REQUEST_BODY.getKey());
            
            // Build and return secure response
            Bundle res = new Bundle();
            res.putString("response", "Success: Data Accessed");
            
            intentGuardManager.setResponse(res).sendResponse();
        }
    }
}

```

## 🎨 UI Customization
The library uses Material 3 (Material You). You can customize the dialog 
content or provide an entirely custom layout.

```java

public class M {
    
    public void test(){
        DialogInfo info = new DialogInfo(
                "Secure Connection Requested",
                "An external application is requesting sensitive data.",
                "Allow Securely",
                "Deny Access"
        );

        intentGuardManager.setDialogInfo(info);
    }
}

```
For Custom XML Layouts:

```java

public class M {

    public void test() {
        intentGuardManager = new IntentGuardManager(this, null, R.layout.my_custom_sheet)
                .registerApproveButton(R.id.btn_ok)
                .registerCancelButton(R.id.btn_no);
    }
}
```

## 🔒 Security Best Practices
* Certificate Pinning: Always use addTrustAppWithCert. Package names can be spoofed; signing certificates cannot.
* Token Expiry: Secure sessions use timed tokens. Handle AuthException.TOKEN_EXPIRED to manage timeout scenarios gracefully.
* UI Integrity: The rationale dialog is non-cancelable by default to prevent "accidental" bypasses.
* Scroll Management: Ensure android:nestedScrollingEnabled="false" is set on custom permission lists (RecyclerView) to prevent scrolling conflicts with the Bottom Sheet.

