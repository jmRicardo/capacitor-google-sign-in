package com.jmricardo.google.auth;

import static org.junit.Assert.assertEquals;

import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialCustomException;
import androidx.credentials.exceptions.GetCredentialInterruptedException;
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException;
import androidx.credentials.exceptions.GetCredentialUnknownException;
import androidx.credentials.exceptions.GetCredentialUnsupportedException;
import androidx.credentials.exceptions.NoCredentialException;
import org.junit.Test;

public class GoogleSignInErrorsTest {

    @Test
    public void cancellationIsCanceled() {
        assertEquals("canceled", GoogleSignInErrors.codeFor(new GetCredentialCancellationException("closed")));
    }

    @Test
    public void interruptedIsNetwork() {
        assertEquals("network", GoogleSignInErrors.codeFor(new GetCredentialInterruptedException("7: NETWORK_ERROR")));
    }

    @Test
    public void noCredentialIsNoCredential() {
        assertEquals("no_credential", GoogleSignInErrors.codeFor(new NoCredentialException("No credentials available")));
    }

    @Test
    public void providerConfigurationAndUnsupportedAreConfigErrors() {
        assertEquals("config_error", GoogleSignInErrors.codeFor(new GetCredentialProviderConfigurationException("missing")));
        assertEquals("config_error", GoogleSignInErrors.codeFor(new GetCredentialUnsupportedException("unsupported")));
    }

    @Test
    public void developerConsoleMismatchIsConfigErrorWhateverTheType() {
        String message = "[28444] Developer console is not set up correctly.";
        assertEquals("config_error", GoogleSignInErrors.codeFor(new NoCredentialException(message)));
        assertEquals("config_error", GoogleSignInErrors.codeFor(new GetCredentialCustomException("custom", message)));
    }

    @Test
    public void anythingElseIsUnknown() {
        assertEquals("unknown", GoogleSignInErrors.codeFor(new GetCredentialUnknownException("boom")));
        assertEquals("unknown", GoogleSignInErrors.codeFor(new GetCredentialCustomException("custom", "boom")));
    }
}
