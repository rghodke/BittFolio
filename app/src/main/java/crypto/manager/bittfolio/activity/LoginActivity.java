package crypto.manager.bittfolio.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String EXTRA_COIN_BALANCE_STRING = "crypto.manager.bittfolio.EXTRA_COIN_BALANCE_STRING";
    private static final String PREFS_LOGIN = "LoginPref";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private EditText mApiKeyView;
    private EditText mApiSecretView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mApiKeyView = (EditText) findViewById(R.id.api_key);
        mApiSecretView = (EditText) findViewById(R.id.api_secret);
        mApiSecretView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        retrieveLoginFromPref();

        Button mBittrexSignInButton = (Button) findViewById(R.id.bittrex_sign_in_button);
        mBittrexSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
    }

    private void retrieveLoginFromPref() {
        SharedPreferences pref = getSharedPreferences(PREFS_LOGIN, MODE_PRIVATE);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);

        if (username == null || password == null) {
            //Prompt for username and password
        } else {
            mApiKeyView.setText(username);
            mApiSecretView.setText(password);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mApiKeyView.setError(null);
        mApiSecretView.setError(null);

        // Store values at the time of the login attempt.
        String api_key = mApiKeyView.getText().toString();
        String api_secret = mApiSecretView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid api_secret, if the user entered one.
        if (TextUtils.isEmpty(api_secret)) {
            mApiSecretView.setError(getString(R.string.error_missing_secret));
            focusView = mApiSecretView;
            cancel = true;
        }

        // Check for a valid api_key address.
        if (TextUtils.isEmpty(api_key)) {
            mApiKeyView.setError(getString(R.string.error_field_required));
            focusView = mApiKeyView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(api_key, api_secret);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void startPortfolioActivity(String s) {
        Intent intent = new Intent(this, PortfolioActivity.class);
        intent.putExtra(EXTRA_COIN_BALANCE_STRING, s);
        startActivity(intent);
    }

    private void persistLoginInPref(String mApiKey, String mApiSecret) {
        Crashlytics.setUserIdentifier(mApiKey);
        getSharedPreferences(PREFS_LOGIN, MODE_PRIVATE)
                .edit()
                .putString(PREF_USERNAME, mApiKey)
                .putString(PREF_PASSWORD, mApiSecret)
                .apply();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mApiKey;
        private final String mApiSecret;

        UserLoginTask(String key, String secret) {
            mApiKey = key;
            mApiSecret = secret;
        }

        //Method imported from
        //https://github.com/platelminto/java-bittrex/blob/master/src/EncryptionUtility.java
        //Used to create the apisign
        public String calculateHash(String secret, String url, String encryption) {

            Mac shaHmac = null;

            try {

                shaHmac = Mac.getInstance(encryption);

            } catch (NoSuchAlgorithmException e) {

                e.printStackTrace();
            }

            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), encryption);

            try {

                shaHmac.init(secretKey);

            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            byte[] hash = shaHmac.doFinal(url.getBytes());
            String check = bytesToHex(hash);

            return check;
        }

        //Method imported from
        //https://github.com/platelminto/java-bittrex/blob/master/src/EncryptionUtility.java
        private String bytesToHex(byte[] bytes) {

            char[] hexArray = "0123456789ABCDEF".toCharArray();

            char[] hexChars = new char[bytes.length * 2];

            for (int j = 0; j < bytes.length; j++) {

                int v = bytes[j] & 0xFF;

                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }

            return new String(hexChars);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String nonce = Long.toString(new Date().getTime());
            URL url = null;
            HttpURLConnection connection = null;
            try {
                String urlString = "https://bittrex.com/api/v1.1/account/getbalances?apikey=" + mApiKey + "&nonce=" + nonce;
                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apisign", calculateHash(mApiSecret, urlString, "HmacSHA512"));

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer resultBuffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null)
                    resultBuffer.append(line);


                int requestCode = connection.getResponseCode();
                if (requestCode == 200) {

                    /*
                    Bittrex will return 200 even if login info is incorrect. Look for success
                    variable
                     */
                    String success = null;
                    try {
                        JSONObject coinBalancesJson = new JSONObject(resultBuffer.toString());
                        success = coinBalancesJson.getString("success");
                    } catch (JSONException e) {
                        success = "false";
                    }
                    if (success.equals("false")) {
                        return false;
                    }
                    //Store for other activities
                    Globals globals = (Globals) getApplication();
                    globals.setApiKey(mApiKey);
                    globals.setApiSecret(mApiSecret);

                    persistLoginInPref(mApiKey, mApiSecret);

                    startPortfolioActivity(resultBuffer.toString());
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.error_signin_message), Toast.LENGTH_SHORT).show();
                mApiSecretView.setError(getString(R.string.error_incorrect_password));
                mApiSecretView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

