package com.intent.guard.core.trusted;

/**
 * Defines the security level used for validating requesting applications.
 * <p>
 * This configuration determines how strictly {@link TrustedRequesterManager}
 * verifies the identity of an incoming intent's source.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
public enum TrustMode {

    /**
     * Identifies applications based solely on their Android package name.
     * <p>
     * <b>Warning:</b> This mode is susceptible to "Package Name Spoofing," where a
     * malicious application uses the same package ID as a trusted partner to intercept
     * data. Use this only during development or for low-risk internal communication.
     * </p>
     * @see #PACKAGE_AND_SIGNATURE
     */
    PACKAGE_NAME_ONLY,

    /**
     * Identifies applications using both their package name and their SHA-256
     * signing certificate fingerprint.
     * <p>
     * <b>Recommended:</b> This is the most secure mode. It ensures that the caller
     * is not only using the correct package name but was also signed by the
     * legitimate developer. This effectively prevents identity spoofing attacks.
     * </p>
     */
    PACKAGE_AND_SIGNATURE
}