package com.intent.guard.core;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Defines the architectural strategy for displaying the authorization UI.
 * <p>
 * This enum allows the developer to choose between a standard window-based dialog
 * or a lifecycle-aware fragment-based dialog.
 * </p>
 *
 * @author David Onyia
 * @since 1.0
 */
public enum DialogType {

    /**
     * Use a {@link BottomSheetDialog}.
     * <p>
     * This is the standard approach for Activity-based flows where the dialog
     * is managed directly by the Activity's window.
     * </p>
     */
    DEFAULT,

    /**
     * Use a {@link BottomSheetDialogFragment}.
     * <p>
     * This approach is recommended when working within a Fragment-driven architecture
     * or when the authorization UI needs to survive configuration changes (like rotation)
     * by leveraging the {@link androidx.fragment.app.FragmentManager}.
     * </p>
     */
    FRAGMENT
}