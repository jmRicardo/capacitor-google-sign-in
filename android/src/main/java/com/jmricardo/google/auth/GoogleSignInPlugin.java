package com.jmricardo.google.auth;

import static android.content.ContentValues.TAG;

import static androidx.core.app.ActivityCompat.startIntentSenderForResult;

import android.app.PendingIntent;
import android.content.IntentSender;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@CapacitorPlugin(name = "GoogleSignIn")
public class GoogleSignInPlugin extends Plugin {

    private GetSignInWithGoogleOption signInWithGoogleOption;
    private CredentialManager credentialManager;
    private final GoogleSignIn googleSignIn = new GoogleSignIn();
    private static final int REQUEST_AUTHORIZE = 1001;

    @PluginMethod
    public void handleSignInButton(PluginCall call) {
        var request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build();

        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> {
            Log.d(TAG, "Credential request was cancelled");
            call.reject("Credential request was cancelled");
        });

        credentialManager.getCredentialAsync(
                this.getContext(),
                request,
                cancellationSignal,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        var data = handleSignIn(result);
                        if (data!=null) {
                            var parsed = googleSignIn.parseCredentialData(data);
                            if (parsed!=null) {
                                call.resolve(parsed);
                            } else {
                                call.reject("Cannot parse credential data");
                            }
                        } else {
                            call.reject("Unexpected type of credential");
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        call.reject("Error! " + e);
                    }
                });
    }

    @PluginMethod
    public void checkScopes(PluginCall call) {
        scopes(call, false);
    }

    @PluginMethod
    public void requestScopes(PluginCall call) {
        scopes(call, true);
    }

    private void scopes(PluginCall call, boolean askForScopes) {
        JSArray jsScopes = call.getArray("scopes", new JSArray());
        String[] scopes = new String[jsScopes.length()];

        for (int i = 0; i < jsScopes.length(); i++) {
            try {
                scopes[i] = jsScopes.getString(i);
            } catch (JSONException e) {
                call.reject("Error parsing scopes", e);
                return;
            }
        }
        List<Scope> requestedScopes = Arrays.stream(scopes).map(Scope::new).collect(Collectors.toList());
        var authorizationRequest = AuthorizationRequest.builder().setRequestedScopes(requestedScopes).build();
        Identity.getAuthorizationClient(this.getActivity())
                .authorize(authorizationRequest)
                .addOnSuccessListener(
                        authorizationResult -> {
                            JSObject response = new JSObject();
                            if (authorizationResult.hasResolution()) {
                                if (askForScopes) requestScopes(authorizationResult);
                                else {
                                    response.put("value", false);
                                    call.reject("Access not granted", response);
                                }
                            } else {
                                response.put("value", true);
                                call.resolve(response);
                            }
                        })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to authorize", e));
    }

    private void requestScopes(AuthorizationResult authorizationResult) {
        // Access needs to be granted by the user
        PendingIntent pendingIntent = authorizationResult.getPendingIntent();
        try {
            startIntentSenderForResult(getActivity(), pendingIntent.getIntentSender(),
                    REQUEST_AUTHORIZE, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Couldn't start Authorization UI: " + e.getLocalizedMessage());
        }
    }


    @Override
    public void load() {
        var config = this.getConfig();
        var serverClientId = config.getString("AndroidServerClientId");
        if(serverClientId != null)
        {
            credentialManager = CredentialManager.create(this.getContext());
            signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(serverClientId)
                    .build();
        }
    }

    public GoogleIdTokenCredential handleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();
        if (credential instanceof CustomCredential customCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                return GoogleIdTokenCredential.createFrom(customCredential.getData());
            } else {
                Log.e(TAG, "Unexpected type of credential");
            }
        } else {
            Log.e(TAG, "Unexpected type of credential");
        }
        return null;
    }


}


