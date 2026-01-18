package com.intent.guard.core.trusted;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton manager responsible for registering and validating trusted applications.
 * <p>
 * This class provides the core identity verification logic for the library. It allows
 * developers to whitelist "partner" apps using two levels of security:
 * <ul>
 * <li><b>Package Name:</b> Simple verification based on the application's unique ID.</li>
 * <li><b>Signature (SHA-256):</b> Cryptographic verification that ensures the caller
 * has been signed with a specific developer certificate, preventing identity spoofing.</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public final class TrustedRequesterManager {

    private static volatile TrustedRequesterManager instance;

    /** Map of trusted apps. Key = package name, Value = set of allowed SHA-256 signatures. */
    private final Map<String, Set<String>> trustedAppsMap
            = Collections.synchronizedMap(new HashMap<>());

    /** Defines the current trust mode (defaults to PACKAGE_NAME_ONLY). */
    private TrustMode trustMode = TrustMode.PACKAGE_NAME_ONLY;

    /**
     * Retrieves the singleton instance of the TrustedRequesterManager.
     *
     * @return The global instance of {@link TrustedRequesterManager}.
     */
    @NonNull
    public static synchronized TrustedRequesterManager getInstance() {
        if (instance == null) {
            instance = new TrustedRequesterManager();
        }
        return instance;
    }

    private TrustedRequesterManager(){
    }

    /**
     * Sets the validation strategy used by {@link #isCallerTrusted(Context, String)}.
     *
     * @param trustMode Use {@link TrustMode#PACKAGE_NAME_ONLY} for flexible testing or
     * {@link TrustMode#PACKAGE_AND_SIGNATURE} for production security.
     */
    public void setTrustMode(@NonNull TrustMode trustMode) {
        this.trustMode = trustMode;
    }

    /**
     * Core validation logic to determine if an external caller is authorized.
     * <p>
     * If the mode is {@code PACKAGE_AND_SIGNATURE}, this method performs an expensive
     * cryptographic lookup to match the caller's APK signing certificate against the
     * registered whitelist.
     * </p>
     *
     * @param context     The application context required to access the {@link PackageManager}.
     * @param packageName The package name of the application attempting to communicate.
     * @return {@code true} if the caller matches a trusted entry; {@code false} otherwise.
     */
    public boolean isCallerTrusted(@NonNull Context context, String packageName){

        Set<String> trustedSignatures = trustedAppsMap.get(packageName);

        if (trustedSignatures == null){
            return false;
        }

        if (getTrustMode() == TrustMode.PACKAGE_NAME_ONLY){
            return true;
        } else if (getTrustMode() == TrustMode.PACKAGE_AND_SIGNATURE) {

            if (trustedSignatures.isEmpty()){
                return false;
            }

            Set<String> callerSignatures = getAppSignatureSha256(context, packageName);

            if (callerSignatures.isEmpty()){
                return false;
            }

            for (String signatures : callerSignatures) {
                if (trustedSignatures.contains(signatures)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extracts the SHA-256 certificate hashes for a given package.
     * <p>Handles the evolution of the Android Signing API, supporting both the
     * legacy Signature API and the modern {@link SigningInfo} API (API 28+).</p>
     */
    @NonNull
    private Set<String> getAppSignatureSha256(Context context, String packageName) {

        Set<String> hashes = ConcurrentHashMap.newKeySet();

        try{
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                SigningInfo signingInfo = packageInfo.signingInfo;
                Signature[] signatures;

                if (signingInfo.hasMultipleSigners()){
                    signatures = signingInfo.getApkContentsSigners();
                } else {
                    signatures = signingInfo.getSigningCertificateHistory();
                }

                for (Signature signature : signatures) {
                    hashes.add(sha256Hex(signature.toByteArray()));
                }

            } else {
                // Legacy support for API < 28
                packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (packageInfo.signatures != null){
                    for (Signature signature : packageInfo.signatures) {
                        hashes.add(sha256Hex(signature.toByteArray()));
                    }
                }
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }

        return hashes;
    }

    /**
     * Converts a raw byte array into a SHA-256 hexadecimal string.
     */
    @NonNull
    private String sha256Hex(byte[] toByteArray) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(toByteArray);
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception exception){
            throw new RuntimeException("SHA-256 not supported", exception);
        }
    }

    /**
     * Registers a package as trusted. This only validates the name, not the signer.
     *
     * @param packageName The unique Android package name (e.g., "com.example.app").
     */
    public void addTrustApp(@NonNull String packageName){
        trustedAppsMap.putIfAbsent(packageName, ConcurrentHashMap.newKeySet());
    }

    /**
     * Registers a package as trusted only if it is signed with a specific certificate.
     *
     * @param packageName The unique Android package name.
     * @param sha256Cert  The SHA-256 hash of the valid signing certificate.
     */
    public void addTrustAppWithCert(@NonNull String packageName, @NonNull String sha256Cert){
        trustedAppsMap.computeIfAbsent(packageName, k -> ConcurrentHashMap.newKeySet()).add(sha256Cert);
    }

    /**
     * Revokes trust for an application and removes it from the internal whitelist.
     *
     * @param packageName The package name to remove.
     */
    public void removeTrustApp(@NonNull String packageName){
        trustedAppsMap.remove(packageName);
    }

    /**
     * Retrieves the current security configuration.
     * @return The active {@link TrustMode}.
     */
    @NonNull
    public TrustMode getTrustMode() {
        return trustMode;
    }
}