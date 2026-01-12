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
        if (loginButton == null || userNameText == null) return;
        updateHeaderUI(loginButton, userNameText);
        loginButton.setOnClickListener(v -> {
            Log.d("Auth0", "Login button clicked");
            login();
        });
    }
//    protected void setupHeaderLogin() {
//        Button loginButton = findViewById(R.id.buttonLogin);
//        if (loginButton == null) return;
//        updateLoginButton(loginButton);
//        loginButton.setOnClickListener(v -> login());
//    }

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

    private void updateHeaderUI(Button loginButton, TextView userNameText) {
        if (userIsAuthenticated) {
            loginButton.setVisibility(View.GONE);

            String idToken = getSharedPreferences(PREFS, MODE_PRIVATE)
                    .getString(KEY_ID_TOKEN, null);

            if (idToken != null) {
                JWT jwt = new JWT(idToken);
                String name = jwt.getClaim("name").asString();

                userNameText.setText(name != null ? name : "User");
                userNameText.setVisibility(View.VISIBLE);

                Log.d("Auth0", "Header updated with user name: " + name);
            }
        } else {
            loginButton.setVisibility(View.VISIBLE);
            userNameText.setVisibility(View.GONE);

            Log.d("Auth0", "User not authenticated – showing LOGIN button");
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

    protected String getAccessToken() {
        return getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_ACCESS_TOKEN, null);
    }


    protected void showSnackBar(String text) {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
    }
}
