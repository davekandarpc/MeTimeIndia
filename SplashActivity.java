package com.metime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.metime.activity.MainActivity;
import com.metime.activity.SettingsActivity;
import com.metime.ad.B2CConfiguration;
import com.metime.ad.B2CUser;
import com.metime.retrofit.ApiManager;
import com.metime.utils.PreferenceHelper;
import com.metime.utils.Utils;
import com.microsoft.identity.client.AcquireTokenParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.Prompt;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private String TAG = "SplashActivity";
    private List<B2CUser> users;

    private IMultipleAccountPublicClientApplication b2cApp;

    Button btnLogin;

    boolean isForLogout = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


       // isForLogout = getIntent().getExtras().containsKey("from") && getIntent().getExtras().getString("from").equals("1");

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setVisibility(View.GONE);

        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createMultipleAccountPublicClientApplication(this,
                R.raw.auth_config_b2c,
                new IPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication application) {
                        b2cApp = application;

                        if (PreferenceHelper.getInstance(SplashActivity.this).getAccessToken().isEmpty()) {
                            AcquireTokenParameters parameters = new AcquireTokenParameters.Builder()
                                    .startAuthorizationFromActivity(SplashActivity.this)
                                    .fromAuthority(B2CConfiguration.getAuthorityFromPolicyName("B2C_1_signin"))
                                    .withScopes(B2CConfiguration.getScopes())
                                    .withPrompt(Prompt.LOGIN)
                                    .withCallback(getAuthInteractiveCallback())
                                    .build();

                            b2cApp.acquireToken(parameters);
                        } else {
                            loadAccounts();
                        }

                        //
                    }

                    @Override
                    public void onError(MsalException exception) {
                        displayError(exception);
                    }
                });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b2cApp == null) {
                    return;
                }
                AcquireTokenParameters parameters = new AcquireTokenParameters.Builder()
                        .startAuthorizationFromActivity(SplashActivity.this)
                        .fromAuthority(B2CConfiguration.getAuthorityFromPolicyName("B2C_1_signin"))
                        .withScopes(B2CConfiguration.getScopes())
                        .withPrompt(Prompt.LOGIN)
                        .withCallback(getAuthInteractiveCallback())
                        .build();

                b2cApp.acquireToken(parameters);

            }
        });
    }

    private void loadAccounts() {
        if (b2cApp == null) {
            return;
        }

        b2cApp.getAccounts(new IPublicClientApplication.LoadAccountsCallback() {
            @Override
            public void onTaskCompleted(final List<IAccount> result) {

                /*if(isForLogout){
                    logout(result);
                    return;
                }*/

                users = B2CUser.getB2CUsersFromAccountList(result);
                b2cApp.acquireTokenSilentAsync(B2CConfiguration.getScopes().toArray(new String[B2CConfiguration.getScopes().size()]),
                        result.get(0), B2CConfiguration.getAuthorityFromPolicyName("B2C_1_signin"),
                        getAuthSilentCallback());
            }

            @Override
            public void onError(MsalException exception) {
                displayError(exception);
            }
        });
    }


    private void displayError(@NonNull final Exception exception) {
        Log.e(TAG, exception.toString());
    }

    private void displayResult(@NonNull final IAuthenticationResult result, boolean isCallAPI) {
        final String output =
                "Access Token :" + result.getAccessToken() + "\n" +
                        "Scope : " + result.getScope() + "\n" +
                        "Expiry : " + result.getExpiresOn() + "\n" +
                        "Tenant ID : " + result.getTenantId() + "\n" +
                        "getId : " + result.getAccount().getId().replace("-b2c_1_signin", "") + "\n";

        PreferenceHelper.getInstance(this).putAccessToken(result.getAccessToken());
        PreferenceHelper.getInstance(this).putObjectId(result.getAccount().getId());

        if (isCallAPI)
            insertUserInformation();
        else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }

        Log.e(TAG, output);
    }

    private void insertUserInformation() {

        Utils.showProgressDialog(this);
        Call<ResponseBody> call = ApiManager.shared(this).service.insertUserInformation();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgressDialog();
                if (response.isSuccessful()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgressDialog();
                t.printStackTrace();
            }
        });
    }

    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated");
                btnLogin.setVisibility(View.GONE);
                /* display result info */
                displayResult(authenticationResult, true);

                /* Reload account asynchronously to get the up-to-date list. */
                //loadAccounts();
            }

            @Override
            public void onError(MsalException exception) {
                btnLogin.setVisibility(View.VISIBLE);
                final String B2C_PASSWORD_CHANGE = "AADB2C90118";
                if (exception.getMessage().contains(B2C_PASSWORD_CHANGE)) {
                    Log.d(TAG, "The user clicks the 'Forgot Password' link in a sign-up or sign-in user flow.\n" +
                            "Your application needs to handle this error code by running a specific user flow that resets the password.");
                    return;
                }

                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                Toast.makeText(SplashActivity.this, "Authentication failed: " + exception.toString(), Toast.LENGTH_LONG).show();
                displayError(exception);

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
                btnLogin.setVisibility(View.VISIBLE);
            }
        };
    }

    private SilentAuthenticationCallback getAuthSilentCallback() {
        return new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.d(TAG, "Successfully authenticated");
                btnLogin.setVisibility(View.GONE);
                /* Successfully got a token. */
                displayResult(authenticationResult, false);
            }

            @Override
            public void onError(MsalException exception) {

                btnLogin.setVisibility(View.VISIBLE);
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

    public void logout(List<IAccount> result){
        for(IAccount i : result) {
            try {
                b2cApp.removeAccount(i);
            } catch (MsalException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        PreferenceHelper.getInstance(this).logout();
    }

}
