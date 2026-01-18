package com.intent.guard.core.internal;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.intent.guard.core.Metadata;
import com.intent.guard.permission.PermissionInfo;
import com.intent.guard.request.RequestPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Internal background worker responsible for parsing requested permissions from incoming Intents.
 * <p>
 * This class implements {@link Runnable} to perform permission lookups on a background thread.
 * It extracts permission identifiers from Intent extras and matches them against the
 * library's defined {@link PermissionInfo} registry.
 * </p>
 *
 * @since 1.0
 * @author David Onyia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class PermissionParser implements Runnable {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final MutableLiveData<List<PermissionInfo>> collectedPermissionList
            = new MutableLiveData<>(new ArrayList<>());

    private final RequestPermission requestPermission
            = RequestPermission.getInstance();

    private volatile boolean isParsing = false;

    @Nullable
    private final Intent requestIntent;

    /**
     * Factory method to create a new parser instance.
     * @param requestIntent The Intent containing {@link Metadata#REQUEST_PERMISSIONS}.
     * @return A fresh PermissionParser instance.
     */
    @NonNull
    public static PermissionParser getInstance(@Nullable Intent requestIntent) {
        return new PermissionParser(requestIntent);
    }

    private PermissionParser(@Nullable Intent requestIntent) {
        this.requestIntent = requestIntent;
    }

    /**
     * Submits the parsing task to the background executor.
     * Note: Ensure observers are attached to {@link #getCollectedPermissionList()}
     * before calling this method.
     */
    public void doParsing(){
        isParsing = true; // Set true immediately on the calling thread
        executorService.submit(this);
    }

    /**
     * @return {@code true} if the background thread is currently processing permissions.
     */
    public boolean isParsing() {
        return isParsing;
    }

    /**
     * @return LiveData containing the parsed {@link PermissionInfo} list.
     */
    @NonNull
    public LiveData<List<PermissionInfo>> getCollectedPermissionList() {
        return collectedPermissionList;
    }

    @Override
    public void run() {
        try {
            if (requestIntent != null && requestIntent.hasExtra(Metadata.REQUEST_PERMISSIONS.getKey())) {

                ArrayList<String> requestPermissions
                        = requestIntent.getStringArrayListExtra(Metadata.REQUEST_PERMISSIONS.getKey());

                List<PermissionInfo> permissionInfos = new ArrayList<>();

                if (requestPermissions != null) {
                    for (String permission : requestPermissions) {
                        PermissionInfo permissionInfo = requestPermission.getDefinePermissionInfo(permission);
                        if (permissionInfo != null) {
                            permissionInfos.add(permissionInfo);
                        }
                    }
                    // Dispatch to Main Thread
                    collectedPermissionList.postValue(permissionInfos);
                }
            }
        } finally {
            // Ensure isParsing is false even if an exception occurs or no extras exist
            isParsing = false;

            // If the list is empty, post an empty list to trigger observers to hide the UI
            if (collectedPermissionList.getValue() == null || collectedPermissionList.getValue().isEmpty()) {
                collectedPermissionList.postValue(new ArrayList<>());
            }
        }
    }
}