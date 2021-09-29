package com.metime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.metime.R;
import com.metime.ad.B2CConfiguration;
import com.metime.ad.B2CUser;
import com.metime.utils.PreferenceHelper;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import java.util.List;

public class RefreshAccessTokenActivity extends AppCompatActivity {

    private String TAG = "RefreshAccessTokenActivity";

    private List<B2CUser> users;

    /* Azure AD Variables */
    private IMultipleAccountPublicClientApplication b2cApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_token);

        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createMultipleAccountPublicClientApplication(this,
                R.raw.auth_config_b2c,
                new IPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication application) {
                        b2cApp = application;
                        loadAccounts();
                    }

                    @Override
                    public void onError(MsalException exception) {
                        displayError(exception);
                    }
                });
    }


    /**
     * Load signed-in accounts, if there's any.
     */
    private void loadAccounts() {
        if (b2cApp == null) {
            return;
        }

        b2cApp.getAccounts(new IPublicClientApplication.LoadAccountsCallback() {
            @Override
            public void onTaskCompleted(final List<IAccount> result) {

                b2cApp.acquireTokenSilentAsync(  B2CConfiguration.getScopes().toArray(new String[B2CConfiguration.getScopes().size()]),
                        result.get(0), B2CConfiguration.getAuthorityFromPolicyName("B2C_1_signin"),
                        getAuthSilentCallback());

               /* users = B2CUser.getB2CUsersFromAccountList(result);
                updateUI(users);*/
            }

            @Override
            public void onError(MsalException exception) {
                displayError(exception);
            }
        });
    }

    /**
     * Updates UI based on the obtained user list.
     */
    private void updateUI(final List<B2CUser> users) {
        if (users.size() != 0) {

        }else{
            startActivity(new Intent(this, LoginActivity.class));
        }

    }

    private void displayError(@NonNull final Exception exception) {
        Log.e(TAG, exception.toString());
    }

    private void displayResult(@NonNull final IAuthenticationResult result) {
        final String output =
                "Access Token :" + result.getAccessToken() + "\n" +
                        "Scope : " + result.getScope() + "\n" +
                        "Expiry : " + result.getExpiresOn() + "\n" +
                        "Tenant ID : " + result.getTenantId() + "\n"+
                        "getId : " + result.getAccount().getId().replace("-b2c_1_signin", "") + "\n";




        PreferenceHelper.getInstance(this).putAccessToken(result.getAccessToken());
        PreferenceHelper.getInstance(this).putObjectId(result.getAccount().getId());

        startActivity(new Intent(RefreshAccessTokenActivity.this, MainActivity.class));
        finish();

        Log.e(TAG, output);
    }

    /**
     * Callback used in for silent acquireToken calls.
     */
    private SilentAuthenticationCallback getAuthSilentCallback() {
        return new SilentAuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.d(TAG, "Successfully authenticated");

                /* Successfully got a token. */
                displayResult(authenticationResult);
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                displayError(exception);

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception instanceof MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }
        };
    }


}
