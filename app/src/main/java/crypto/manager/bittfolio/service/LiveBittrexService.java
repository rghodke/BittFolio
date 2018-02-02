package crypto.manager.bittfolio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LiveBittrexService extends Service {
    // Random number generator
    private static final String LIVE_COIN_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_COIN_INTENT_EXTRA";
    private static final String LIVE_COIN_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_COIN_INTENT_ACTION";
    private static final String LIVE_ORDER_BOOK_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_ORDER_BOOK_INTENT_EXTRA";
    private static final String LIVE_ORDER_BOOK_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_ORDER_BOOK_INTENT_ACTION";
    private static final String LIVE_CLOSED_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_CLOSED_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_CLOSED_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_CLOSED_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_OPEN_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_OPEN_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_OPEN_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_OPEN_ORDER_HISTORY_INTENT_ACTION";
    private static final String API_KEY = "crypto.manager.bittfolio.API_KEY";
    private static final String API_SECRET = "crypto.manager.bittfolio.API_SECRET";
    private static final String CURRENCY = "crypto.manager.bittfolio.CURRENCY";
    private static final String LATEST_PRICE_INTENT_ACTION = "crypto.manager.bittfolio.LATEST_PRICE_INTENT_ACTION";
    private static final String LATEST_PRICE_INTENT_EXTRA = "crypto.manager.bittfolio.LATEST_PRICE_INTENT_EXTRA";
    private static final String LIVE_PRICE_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_PRICE_HISTORY_INTENT_EXTRA";
    private static final String LIVE_PRICE_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_PRICE_HISTORY_INTENT_ACTION";
    private static final String LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA";
    private static final String LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION";
    private static final String LATEST_BTC_USDT_PRICE_INTENT_EXTRA = "crypto.manager.bittfolio.LATEST_BTC_USDT_PRICE_INTENT_EXTRA";
    private static final String LATEST_BTC_USDT_PRICE_INTENT_ACTION = "crypto.manager.bittfolio.LATEST_BTC_USDT_PRICE_INTENT_ACTION";
    private static final String LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_CURRENT_HOLDINGS_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_CURRENT_HOLDINGS_INTENT_EXTRA";
    private static final String LIVE_CURRENT_HOLDINGS_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_CURRENT_HOLDINGS_INTENT_ACTION";
    private static final String LIVE_WITHDRAW_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_WITHDRAW_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_DEPOSIT_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_DEPOSIT_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_WITHDRAW_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_WITHDRAW_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_DEPOSIT_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_DEPOSIT_ORDER_HISTORY_INTENT_EXTRA";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Binder given to clients
    // Random number generator
    private Context context;
    private String mApiKey, mApiSecret, mCurrency;
    private OkHttpClient client;

    @Override
    public IBinder onBind(Intent intent) {
        client = new OkHttpClient();
        mApiKey = intent.getExtras().getString(API_KEY);
        mApiSecret = intent.getExtras().getString(API_SECRET);
        mCurrency = intent.getExtras().getString(CURRENCY);
        context = this;
        return mBinder;
    }

    /**
     * methods for clients
     */
    public void getLiveCoinValues() {
        connectBittrexPublicApi("getmarketsummaries", LIVE_COIN_INTENT_EXTRA, LIVE_COIN_INTENT_ACTION);
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

    public void getClosedOrderHistory() {

        String endpoint = "account/getorderhistory";
        String urlParams = "market=" + "BTC-" + mCurrency;
        Callback closedOrderHistory = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_CLOSED_ORDER_HISTORY_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_CLOSED_ORDER_HISTORY_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        connectBittrexPrivate(endpoint, urlParams, closedOrderHistory);
    }

    public void getCurrentHoldings() {
        String endpoint = "account/getbalances";
        String urlParams = "";
        Callback currentHoldingCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_CURRENT_HOLDINGS_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_CURRENT_HOLDINGS_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        connectBittrexPrivate(endpoint, urlParams, currentHoldingCallback);
    }

    public void getOverallClosedOrderHistory() {

        String endpoint = "account/getorderhistory";
        String urlParams = "";
        Callback closedOrderHistory = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        connectBittrexPrivate(endpoint, urlParams, closedOrderHistory);
    }

    public void getOverallOpenOrderHistory() {
        String endpoint = "market/getopenorders";
        Callback overallOpenOrderHistory = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        String urlParams = "";
        connectBittrexPrivate(endpoint, urlParams, overallOpenOrderHistory);
    }

    public void getOpenOrderHistory() {

        String endpoint = "market/getopenorders";
        String urlParams = "market=" + "BTC-" + mCurrency;
        Callback openOrderHistory = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_OPEN_ORDER_HISTORY_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_OPEN_ORDER_HISTORY_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        connectBittrexPrivate(endpoint, urlParams, openOrderHistory);
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


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("Response").equals("Success") && jsonObject.getInt("Type") >= 100) {
                            Intent intent = new Intent();
                            intent.putExtra(intentExtra, responseString);
                            intent.setAction(intentAction);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void getDepositTransferHistory() {
        String endpoint = "account/getdeposithistory";
        String urlParams = "currency=" + mCurrency;
        Callback closedOrderHistory = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_DEPOSIT_ORDER_HISTORY_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_DEPOSIT_ORDER_HISTORY_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        connectBittrexPrivate(endpoint, urlParams, closedOrderHistory);
    }

    public void getWithdrawTransferHistory() {
        String endpoint = "account/getwithdrawalhistory";
        String urlParams = "currency=" + mCurrency;
        Callback closedOrderHistory = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_WITHDRAW_ORDER_HISTORY_INTENT_EXTRA, responseString);
                            intent.setAction(LIVE_WITHDRAW_ORDER_HISTORY_INTENT_ACTION);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        connectBittrexPrivate(endpoint, urlParams, closedOrderHistory);
    }

    public void getLatestPrice() {
        connectBittrexPublicApi("getticker?market=BTC-" + mCurrency, LATEST_PRICE_INTENT_EXTRA, LATEST_PRICE_INTENT_ACTION);
    }

    public void getUSDTBTCPrice() {
        connectBittrexPublicApi("getticker?market=USDT-BTC", LATEST_BTC_USDT_PRICE_INTENT_EXTRA, LATEST_BTC_USDT_PRICE_INTENT_ACTION);
    }

    ;

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
                    String responseString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(intentExtra, responseString);
                            intent.setAction(intentAction);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void connectBittrexPrivate(String endpoint, String urlParameters, Callback callback) {
        String nonce = Long.toString(new Date().getTime());
        String apiKey = mApiKey;
        String apiSecret = mApiSecret;
        String urlString = "https://bittrex.com/api/v1.1/" + endpoint + "?apikey=" + apiKey + "&nonce=" + nonce + "&" + urlParameters;
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .addHeader("apisign", calculateHash(apiSecret, urlString, "HmacSHA512"))
                .build();

        client.newCall(request).enqueue(callback);

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
}