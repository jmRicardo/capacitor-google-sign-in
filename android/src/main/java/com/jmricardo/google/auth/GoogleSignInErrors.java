package com.jmricardo.google.auth;

import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialInterruptedException;
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException;
import androidx.credentials.exceptions.GetCredentialUnsupportedException;
import androidx.credentials.exceptions.NoCredentialException;

/** Values of `error.code` on the JS side. Mirrors `GoogleSignInErrorCode` in definitions.ts. */
final class GoogleSignInErrors {

    static final String CANCELED = "canceled";
    static final String NO_CREDENTIAL = "no_credential";
    static final String CONFIG_ERROR = "config_error";
    static final String NETWORK = "network";
    static final String UNKNOWN = "unknown";

    private GoogleSignInErrors() {}

    static String codeFor(GetCredentialException e) {
        if (e instanceof GetCredentialCancellationException) {
            return CANCELED;
        }
        // Play services turns NETWORK_ERROR and CONNECTION_SUSPENDED_DURING_CALL into this one.
        if (e instanceof GetCredentialInterruptedException) {
            return NETWORK;
        }
        if (e instanceof GetCredentialProviderConfigurationException || e instanceof GetCredentialUnsupportedException) {
            return CONFIG_ERROR;
        }
        // A client id or SHA-1 that does not match the Cloud console comes back as "no credential"
        // (or a custom exception); only the message tells it apart.
        if (isDeveloperConsoleError(e)) {
            return CONFIG_ERROR;
        }
        if (e instanceof NoCredentialException) {
            return NO_CREDENTIAL;
        }
        return UNKNOWN;
    }

    private static boolean isDeveloperConsoleError(GetCredentialException e) {
        String message = e.getMessage();
        return message != null && message.contains("Developer console is not set up correctly");
    }
}
