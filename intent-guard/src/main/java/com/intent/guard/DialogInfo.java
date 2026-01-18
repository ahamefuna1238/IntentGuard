package com.intent.guard;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Data model responsible for customizing the text content shown in the authorization dialog.
 * <p>
 * This class allows developers to override default strings for the header, body, and action
 * buttons. It uses the Builder pattern to provide a fluent API for configuration.
 * If a field is not explicitly set, the class automatically falls back to the library's
 * default string resources.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 *     DialogInfo dialogInfo = DialogInfo.builder(this)
 *          .setHeaderText("Secure Access Request")
 *          .setBodyText("This app wants to access your data.")
 *          .setAcceptButtonText("Allow")
 *          .setCancelButtonText("Deny")
 *          .build();
 * }</pre>
 *
 * @since 1.0
 * @author David Onyia
 */
public final class DialogInfo {

    @Nullable
    private final String headerText;
    @Nullable
    private final String bodyText;
    @Nullable
    private final String cancelButtonText;
    @Nullable
    private final String acceptButtonText;
    @NonNull
    private final Context context;

    private DialogInfo(@NonNull Context context, @Nullable String headerText, @Nullable String bodyText, @Nullable String cancelButtonText, @Nullable String acceptButtonText){
        this.context = context;
        this.headerText = headerText;
        this.bodyText = bodyText;
        this.cancelButtonText = cancelButtonText;
        this.acceptButtonText = acceptButtonText;
    }

    /**
     * @return The customized approval button text, or the default "Approve Request" string if null.
     */
    @NonNull
    public String getAcceptButtonText() {
        return acceptButtonText != null ? acceptButtonText
                : context.getResources().getString(R.string.m_approve_trusted_text);
    }

    /**
     * @return The customized body/description text, or the default placeholder if null.
     */
    @NonNull
    public String getBodyText() {
        return bodyText != null ? bodyText
                : context.getResources().getString(R.string.m_trusted_requester_name_unavailable_requesting_description);
    }

    /**
     * @return The customized cancel button text, or the default "Cancel Request" string if null.
     */
    @NonNull
    public String getCancelButtonText() {
        return cancelButtonText != null ? cancelButtonText
                : context.getResources().getString(R.string.m_cancel_text);
    }

    /**
     * @return The customized header title, or the default string if null.
     */
    @NonNull
    public String getHeaderText() {
        return headerText != null ? headerText :
                context.getResources().getString(R.string.m_requester_name_unavailable_request_header);
    }

    /**
     * Entry point to create a new Builder instance.
     * @param context The context used to resolve default string resources.
     * @return A new {@link Builder} instance.
     */
    @NonNull
    public static Builder builder(@NonNull Context context){
        return new Builder(context);
    }

    /**
     * Builder class for {@link DialogInfo}.
     * <p>Provides a fluent interface to set optional text fields for the authorization dialog.</p>
     */
    public static final class Builder {
        @Nullable
        private String headerText;
        @Nullable
        private String bodyText;
        @Nullable
        private String cancelButtonText;
        @Nullable
        private String acceptButtonText;
        @NonNull
        private final Context context;

        /**
         * @param context Context required for resource resolution during the build process.
         */
        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * Sets the title text displayed at the top of the authorization dialog.
         * @param headerText The title string.
         * @return The current Builder instance.
         */
        public Builder setHeaderText(@NonNull String headerText) {
            this.headerText = headerText;
            return this;
        }

        /**
         * Sets the descriptive text explaining the purpose of the request to the user.
         * @param bodyText The description string.
         * @return The current Builder instance.
         */
        public Builder setBodyText(@NonNull String bodyText) {
            this.bodyText = bodyText;
            return this;
        }

        /**
         * Customizes the text on the negative/deny button.
         * @param cancelButtonText The label for the cancel button.
         * @return The current Builder instance.
         */
        public Builder setCancelButtonText(@NonNull String cancelButtonText) {
            this.cancelButtonText = cancelButtonText;
            return this;
        }

        /**
         * Customizes the text on the positive/allow button.
         * @param acceptButtonText The label for the approval button.
         * @return The current Builder instance.
         */
        public Builder setAcceptButtonText(@NonNull String acceptButtonText) {
            this.acceptButtonText = acceptButtonText;
            return this;
        }

        /**
         * Constructs the {@link DialogInfo} object with the provided values.
         * @return A fully initialized DialogInfo instance.
         */
        @NonNull
        public DialogInfo build(){
            return new DialogInfo(context, headerText, bodyText, cancelButtonText, acceptButtonText);
        }
    }
}