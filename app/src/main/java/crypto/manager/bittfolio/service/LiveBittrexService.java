package crypto.manager.bittfolio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LiveBittrexService extends Service {
    // Random number generator
    private static final String LIVE_COIN_INTENT_EXTRA = "LIVE_COIN_INTENT_EXTRA";
    private static final String LIVE_COIN_INTENT_ACTION = "LIVE_COIN_INTENT_ACTION";
    private static final String LIVE_ORDER_BOOK_INTENT_EXTRA = "LIVE_ORDER_BOOK_INTENT_EXTRA";
    private static final String LIVE_ORDER_BOOK_INTENT_ACTION = "LIVE_ORDER_BOOK_INTENT_ACTION";
    private static final String LIVE_ORDER_HISTORY_INTENT_EXTRA = "LIVE_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_ORDER_HISTORY_INTENT_ACTION = "LIVE_ORDER_HISTORY_INTENT_ACTION";
    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String CURRENCY = "CURRENCY";
    private static final String LATEST_PRICE_INTENT_ACTION = "LATEST_PRICE_INTENT_ACTION";
    private static final String LATEST_PRICE_INTENT_EXTRA = "LATEST_PRICE_INTENT_EXTRA";
    private static final String LIVE_PRICE_HISTORY_INTENT_EXTRA = "LIVE_PRICE_HISTORY_INTENT_EXTRA";
    private static final String LIVE_PRICE_HISTORY_INTENT_ACTION = "LIVE_PRICE_HISTORY_INTENT_ACTION";
    private static final String LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA = "LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA";
    private static final String LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION = "LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Binder given to clients
    // Random number generator
    private final Random mGenerator = new Random();
    private final Context context = this;

    private String mApiKey, mApiSecret, mCurrency;
    private OkHttpClient client;
    private Object latestPrice;
    private Object minuteDataForOneHour;

    @Override
    public IBinder onBind(Intent intent) {
        client = new OkHttpClient();
        mApiKey = intent.getExtras().getString(API_KEY);
        mApiSecret = intent.getExtras().getString(API_SECRET);
        mCurrency = intent.getExtras().getString(CURRENCY);
        return mBinder;
    }

    /**
     * methods for clients
     */
    public void getLiveCoinValues() {
        connectBittrexPublicApi("getmarketsummaries", LIVE_COIN_INTENT_EXTRA, LIVE_COIN_INTENT_ACTION);
    }

    public void getOrderHistory() {
        new OrderHistoryTask().execute();
    }

    public void getOrderBook() {
        connectBittrexPublicApi("getorderbook?market=BTC-" + mCurrency + "&type=both", LIVE_ORDER_BOOK_INTENT_EXTRA, LIVE_ORDER_BOOK_INTENT_ACTION);
    }

    public void getMinuteDataForOneHour() {
        connectCryptoComparePublicApi("histominute?fsym=" + mCurrency + "&tsym=BTC&limit=60&aggregate=1&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getHalfHourDataForPast12Hours() {
        connectCryptoComparePublicApi("histominute?fsym=" + mCurrency + "&tsym=BTC&limit=24&aggregate=30&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getHourlyDataForPast24Hours() {
        connectCryptoComparePublicApi("histohour?fsym=" + mCurrency + "&tsym=BTC&limit=24&aggregate=1&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getThreeHourlyDataForPast3Days() {
        connectCryptoComparePublicApi("histohour?fsym=" + mCurrency + "&tsym=BTC&limit=24&aggregate=3&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getSixHourlyDataForPast1Week() {
        connectCryptoComparePublicApi("histohour?fsym=" + mCurrency + "&tsym=BTC&limit=28&aggregate=6&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getDailyDataForPast1Month() {
        connectCryptoComparePublicApi("histoday?fsym=" + mCurrency + "&tsym=BTC&limit=30&aggregate=1&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getThreeDaysDataForPast3Month() {
        connectCryptoComparePublicApi("histoday?fsym=" + mCurrency + "&tsym=BTC&limit=30&aggregate=3&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }

    public void getWeeklyDataForPast6Month() {
        connectCryptoComparePublicApi("histoday?fsym=" + mCurrency + "&tsym=BTC&limit=26&aggregate=7&e=Bittrex", LIVE_PRICE_HISTORY_INTENT_EXTRA, LIVE_PRICE_HISTORY_INTENT_ACTION);
    }


    public void getMarketDataForCurrency() {
        connectBittrexPublicApi("getmarketsummary?market=BTC-" + mCurrency, LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA, LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION);
    }

    private void connectCryptoComparePublicApi(String publicParameter, final String intentExtra, final String intentAction) {
        Request request = new Request.Builder()
                .url("https://min-api.cryptocompare.com/data/" + publicParameter)
                .build();

        System.out.println(request.url().toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String currenciesJSONString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(currenciesJSONString);
                        if (jsonObject.getString("Response").equals("Success") && jsonObject.getInt("Type") >= 100) {
                            Intent intent = new Intent();
                            intent.putExtra(intentExtra, currenciesJSONString);
                            intent.setAction(intentAction);
                            context.sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    private void connectBittrexPublicApi(String publicParameter, final String intentExtra, final String intentAction) {
        Request request = new Request.Builder()
                .url("https://bittrex.com/api/v1.1/public/" + publicParameter)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String currenciesJSONString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(currenciesJSONString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(intentExtra, currenciesJSONString);
                            intent.setAction(intentAction);
                            context.sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void getLatestPrice() {
        connectBittrexPublicApi("getticker?market=BTC-" + mCurrency, LATEST_PRICE_INTENT_EXTRA, LATEST_PRICE_INTENT_ACTION);
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LiveBittrexService getService() {
            // Return this instance of LiveBittrexService so clients can call public methods
            return LiveBittrexService.this;
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
                    if (success.equals("false")) {
                        return "";
                    }

                    return resultBuffer.toString();
                } else {
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
                intent.putExtra(LIVE_ORDER_HISTORY_INTENT_EXTRA, success);
                intent.setAction(LIVE_ORDER_HISTORY_INTENT_ACTION);
                context.sendBroadcast(intent);
            } else {
            }
        }
    }
}