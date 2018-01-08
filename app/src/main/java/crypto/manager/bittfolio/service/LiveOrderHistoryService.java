package crypto.manager.bittfolio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

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
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LiveOrderHistoryService extends Service {
    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String CURRENCY = "CURRENCY";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    private final Context context = this;
    private final String LIVE_ORDER_HISTORY_EXTRA = "LIVE_ORDER_HISTORY_EXTRA";
    private final String LIVE_ORDER_HISTORY_INTENT_ACTION = "LIVE_ORDER_HISTORY_INTENT_ACTION";
    private String mApiKey, mApiSecret, mCurrency;

    @Override
    public IBinder onBind(Intent intent) {
        mApiKey = intent.getExtras().getString(API_KEY);
        mApiSecret = intent.getExtras().getString(API_SECRET);
        mCurrency = intent.getExtras().getString(CURRENCY);
        return mBinder;
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

    /**
     * method for clients
     */
    public void getOrderHistory() {
        new OrderHistoryTask().execute();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LiveOrderHistoryService getService() {
            // Return this instance of LiveCoinValueService so clients can call public methods
            return LiveOrderHistoryService.this;
        }
    }

    //Ripped from LoginActivity
    public class OrderHistoryTask extends AsyncTask<Void, Void, String> {

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
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String nonce = Long.toString(new Date().getTime());
            URL url = null;
            HttpURLConnection connection = null;
            try {
                String urlString = "https://bittrex.com/api/v1.1/account/getorderhistory?apikey=" + mApiKey + "&nonce=" + nonce + "&market=" + "BTC-" + mCurrency;
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
                        JSONObject successJSON = new JSONObject(resultBuffer.toString());
                        success = successJSON.getString("success");
                    } catch (JSONException e) {
                        success = "false";
                    }
                    if(success.equals("false")){
                        return "";
                    }

                    return resultBuffer.toString();
                }
                else{
                    return "";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }

        }

        @Override
        protected void onPostExecute(final String success) {
            if (!success.isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra(LIVE_ORDER_HISTORY_EXTRA, success);
                intent.setAction(LIVE_ORDER_HISTORY_INTENT_ACTION);
                context.sendBroadcast(intent);
            } else {
            }
        }

    }

}



