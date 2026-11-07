package com.jmricardo.google.auth;

import android.app.Activity;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CredentialOption;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.ClearCredentialProviderConfigurationException;
import androidx.credentials.exceptions.GetCredentialException;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@CapacitorPlugin(name = "GoogleSignIn")
public class GoogleSignInPlugin extends Plugin {

    private CredentialManager credentialManager;
    private String serverClientId;
    // Callbacks only build a JSObject: the main thread is enough and nothing leaks per call.
    private Executor callbackExecutor;
    private final AtomicBoolean signInInFlight = new AtomicBoolean(false);

    @Override
    public void load() {
        credentialManager = CredentialManager.create(getContext());
        serverClientId = getConfig().getString("AndroidServerClientId");
        callbackExecutor = ContextCompat.getMainExecutor(getContext());
    }

    @PluginMethod
    public void signIn(PluginCall call) {
        String nonce = call.getString("nonce");
        if (isEmpty(nonce)) {
            call.reject("signIn needs a nonce issued by your backend.", GoogleSignInErrors.CONFIG_ERROR);
            return;
        }
        if (isEmpty(serverClientId)) {
            call.reject("Set plugins.GoogleSignIn.AndroidServerClientId in the Capacitor config.", GoogleSignInErrors.CONFIG_ERROR);
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("There is no activity to show the sign-in from.", GoogleSignInErrors.UNKNOWN);
            return;
        }

        CredentialOption option;
        try {
            option = buildOption(call, nonce);
        } catch (IllegalArgumentException e) {
            call.reject(e.getMessage(), GoogleSignInErrors.CONFIG_ERROR);
            return;
        }

        if (!signInInFlight.compareAndSet(false, true)) {
            call.reject("Another sign-in is already in progress.", GoogleSignInErrors.UNKNOWN);
            return;
        }
        try {
            GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(option).build();
            credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                callbackExecutor,
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        signInInFlight.set(false);
                        resolveCredential(call, response.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        signInInFlight.set(false);
                        call.reject(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), GoogleSignInErrors.codeFor(e));
                    }
                }
            );
        } catch (RuntimeException e) {
            signInInFlight.set(false);
            call.reject("Could not start the sign-in: " + e.getMessage(), GoogleSignInErrors.UNKNOWN);
        }
    }

    @PluginMethod
    public void signOut(PluginCall call) {
        credentialManager.clearCredentialStateAsync(
            new ClearCredentialStateRequest(),
            null,
            callbackExecutor,
            new CredentialManagerCallback<>() {
                @Override
                public void onResult(Void result) {
                    call.resolve();
                }

                @Override
                public void onError(ClearCredentialException e) {
                    String code =
                        e instanceof ClearCredentialProviderConfigurationException
                            ? GoogleSignInErrors.CONFIG_ERROR
                            : GoogleSignInErrors.UNKNOWN;
                    call.reject(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), code);
                }
            }
        );
    }

    private CredentialOption buildOption(PluginCall call, String nonce) {
        String hostedDomain = call.getString("hostedDomain");
        JSObject android = call.getObject("android", new JSObject());
        String flow = android.getString("flow", "button");

        if ("bottomSheet".equals(flow)) {
            GetGoogleIdOption.Builder builder = new GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setNonce(nonce)
                .setFilterByAuthorizedAccounts(Boolean.TRUE.equals(android.getBoolean("filterByAuthorizedAccounts", false)))
                .setAutoSelectEnabled(Boolean.TRUE.equals(android.getBoolean("autoSelect", false)));
            if (!isEmpty(hostedDomain)) {
                builder.setHostedDomainFilter(hostedDomain);
            }
            return builder.build();
        }
        if (!"button".equals(flow)) {
            throw new IllegalArgumentException("android.flow must be 'button' or 'bottomSheet', not '" + flow + "'.");
        }
        GetSignInWithGoogleOption.Builder builder = new GetSignInWithGoogleOption.Builder(serverClientId).setNonce(nonce);
        if (!isEmpty(hostedDomain)) {
            builder.setHostedDomainFilter(hostedDomain);
        }
        return builder.build();
    }

    private void resolveCredential(PluginCall call, Credential credential) {
        if (
            !(credential instanceof CustomCredential custom) ||
            !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())
        ) {
            call.reject("Credential Manager returned an unexpected credential type.", GoogleSignInErrors.UNKNOWN);
            return;
        }
        GoogleIdTokenCredential google;
        try {
            google = GoogleIdTokenCredential.createFrom(custom.getData());
        } catch (RuntimeException e) {
            call.reject("Could not read the Google credential.", GoogleSignInErrors.UNKNOWN);
            return;
        }
        // Deliberately not logged: it carries the ID token, and logcat is readable over adb.
        JSObject result = new JSObject();
        result.put("idToken", google.getIdToken());
        result.put("email", orEmpty(google.getEmail()));
        result.put("givenName", orEmpty(google.getGivenName()));
        result.put("familyName", orEmpty(google.getFamilyName()));
        result.put("serverAuthCode", "");
        call.resolve(result);
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
