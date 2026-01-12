package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.auth0.android.jwt.JWT;
import androidx.appcompat.app.AppCompatActivity;
import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseActivity extends AppCompatActivity {

    protected Auth0 account;
    protected boolean userIsAuthenticated = false;

    private static final String PREFS = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ID_TOKEN = "id_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        account = new Auth0(
                getString(R.string.com_auth0_client_id),
                getString(R.string.com_auth0_domain)
        );

        restoreSession();
    }

    /**
     * Must be called AFTER setContentView() in child activities
     */
    protected void setupHeaderLogin() {
        Button loginButton = findViewById(R.id.buttonLogin);
        TextView userNameText = findViewById(R.id.textUserName);
        TextView logoutText = findViewById(R.id.textLogout);

        if (loginButton == null || userNameText == null || logoutText == null) return;

        updateHeaderUI(loginButton, userNameText, logoutText);

        loginButton.setOnClickListener(v -> {
            Log.d("Auth0", "Login button clicked");
            login();
        });

        logoutText.setOnClickListener(v -> {
            Log.d("Auth0", "Logout clicked");
            logout();
        });
    }


    private void login() {
        Log.d("Auth0", "Starting Auth0 login flow");

        WebAuthProvider.login(account)
                .withScheme(getString(R.string.com_auth0_scheme))
                .withAudience("https://booking-backend-295607ecab74.herokuapp.com/")
                .withScope("openid profile email")
                .start(this, new Callback<Credentials, AuthenticationException>() {
                    @Override
                    public void onSuccess(Credentials credentials) {
                        Log.d("Auth0", "Login SUCCESS");

                        saveSession(credentials);
                        userIsAuthenticated = true;

                        // Decode ID token to get user info
                        JWT jwt = new JWT(credentials.getIdToken());
                        String name = jwt.getClaim("name").asString();
                        String email = jwt.getClaim("email").asString();

                        Log.d("Auth0", "User name: " + name);
                        Log.d("Auth0", "User email: " + email);

                        showSnackBar("Welcome " + (name != null ? name : ""));
                        recreate(); // refresh UI
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        Log.e("Auth0", "Login FAILED");
                        Log.e("Auth0", "Error: " + exception.getDescription(), exception);
                        showSnackBar("Login failed");
                    }
                });
    }

    private void logout() {
        Log.d("Auth0", "Starting logout");

        WebAuthProvider.logout(account)
                .withScheme(getString(R.string.com_auth0_scheme))
                .start(this, new Callback<Void, AuthenticationException>() {
                    @Override
                    public void onSuccess(Void payload) {
                        Log.d("Auth0", "Logout SUCCESS");

                        clearSession();
                        userIsAuthenticated = false;

                        showSnackBar("Logged out");
                        recreate(); // refresh UI
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        Log.e("Auth0", "Logout FAILED: " + exception.getCode());
                        showSnackBar("Logout failed");
                    }
                });
    }


    private void updateHeaderUI(
            Button loginButton,
            TextView userNameText,
            TextView logoutText
    ) {
        if (userIsAuthenticated) {
            loginButton.setVisibility(View.GONE);

            String idToken = getSharedPreferences(PREFS, MODE_PRIVATE)
                    .getString(KEY_ID_TOKEN, null);

            String name = "User";
            if (idToken != null) {
                JWT jwt = new JWT(idToken);
                name = jwt.getClaim("name").asString() != null
                        ? jwt.getClaim("name").asString()
                        : "User";
            }

            userNameText.setText(name);
            userNameText.setVisibility(View.VISIBLE);
            logoutText.setVisibility(View.VISIBLE);

            Log.d("Auth0", "Header → Logged in as " + name);

        } else {
            loginButton.setVisibility(View.VISIBLE);
            userNameText.setVisibility(View.GONE);
            logoutText.setVisibility(View.GONE);

            Log.d("Auth0", "Header → Logged out");
        }
    }



    private void saveSession(Credentials credentials) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, credentials.getAccessToken())
                .putString(KEY_ID_TOKEN, credentials.getIdToken())
                .apply();
    }

    private void restoreSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        userIsAuthenticated = prefs.contains(KEY_ACCESS_TOKEN);
    }

    private void clearSession() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Log.d("Auth0", "Local session cleared");
    }


    protected String getAccessToken() {
        return getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_ACCESS_TOKEN, null);
    }


    protected void showSnackBar(String text) {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
    }
}
