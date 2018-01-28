package crypto.manager.bittfolio.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.CoinGraphFragment;
import crypto.manager.bittfolio.fragment.OrderBookFragment;
import crypto.manager.bittfolio.fragment.OrderFragment;
import crypto.manager.bittfolio.fragment.OrderHistoryFragment;
import crypto.manager.bittfolio.fragment.TransferFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.model.OrderHistoryEntry;
import crypto.manager.bittfolio.service.LiveBittrexService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoinDataActivity extends AppCompatActivity implements CoinGraphFragment.OnCoinGraphFragmentInteractionListener, OrderHistoryFragment.OnOrderHistoryListFragmentInteractionListener, OrderFragment.OrderFragmentInteractionListener, OrderBookFragment.OrderBookFragmentInteractionListener, TransferFragment.TransferFragmentInteractionListener {

    private static final String API_KEY = "crypto.manager.bittfolio.API_KEY";
    private static final String API_SECRET = "crypto.manager.bittfolio.API_SECRET";
    private static final String CURRENCY = "crypto.manager.bittfolio.CURRENCY";
    private static final String ARG_COIN_DATA = "crypto.manager.bittfolio.ARG_COIN_DATA";
    private static final String LIVE_ORDER_BOOK_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_ORDER_BOOK_INTENT_EXTRA";
    private static final String LIVE_ORDER_BOOK_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_ORDER_BOOK_INTENT_ACTION";
    private static final String LATEST_PRICE_INTENT_ACTION = "crypto.manager.bittfolio.LATEST_PRICE_INTENT_ACTION";
    private static final String LATEST_PRICE_INTENT_EXTRA = "crypto.manager.bittfolio.LATEST_PRICE_INTENT_EXTRA";
    private static final String LIVE_CLOSED_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_CLOSED_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_CLOSED_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_CLOSED_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_OPEN_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_OPEN_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_OPEN_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_OPEN_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_PRICE_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_PRICE_HISTORY_INTENT_EXTRA";
    private static final String LIVE_PRICE_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_PRICE_HISTORY_INTENT_ACTION";
    private static final String LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA";
    private static final String LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION";
    private static final String LATEST_BTC_USDT_PRICE_INTENT_EXTRA = "crypto.manager.bittfolio.LATEST_BTC_USDT_PRICE_INTENT_EXTRA";
    private static final String LATEST_BTC_USDT_PRICE_INTENT_ACTION = "crypto.manager.bittfolio.LATEST_BTC_USDT_PRICE_INTENT_ACTION";
    private CoinData mCoinData;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private LiveBittrexService mService;
    private ServiceConnection mConnection;
    private boolean mBound = false;
    private BroadcastReceiver mBroadCastNewMessage;
    private OrderHistoryFragment mOrderHistoryFragment;
    private OrderFragment mOrderFragment;
    private OrderBookFragment mOrderBookFragment;
    private Handler mOrderBookHandler;
    private Handler mOrderHistoryHandler;
    private TransferFragment mTransferFragment;
    private Handler mPriceHandler;
    private CoinGraphFragment mCoinGraphFragment;
    private Handler mCoinGraph;
    private Handler mBtcUsdtHandler;
    private OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_data);

        mClient = new OkHttpClient();

        mCoinData = getIntent().getExtras().getParcelable(ARG_COIN_DATA);

        getSupportActionBar().setTitle(mCoinData.getCurrency());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Set up the different tabs
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coin_data, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void updateCoinGraph(final int i) {
        //Every second get the newest info about the coin you want
        mCoinGraph = new Handler();
        final int delay = 1000; //milliseconds

        mCoinGraph.postDelayed(new Runnable() {
            public void run() {
                mService.getMarketDataForCurrency();
                //do something
                switch (i) {
                    case 0:
                        mService.getMinuteDataForOneHour();
                        break;
                    case 1:
                        mService.getHalfHourDataForPast12Hours();
                        break;
                    case 2:
                        mService.getHourlyDataForPast24Hours();
                        break;
                    case 3:
                        mService.getThreeHourlyDataForPast3Days();
                        break;
                    case 4:
                        mService.getSixHourlyDataForPast1Week();
                        break;
                    case 5:
                        mService.getDailyDataForPast1Month();
                        break;
                    case 6:
                        mService.getThreeDaysDataForPast3Month();
                        break;
                    case 7:
                        mService.getWeeklyDataForPast6Month();
                        break;
                    default:
                        mService.getHourlyDataForPast24Hours();
                        break;
                }
                mCoinGraph.postDelayed(this, delay);

            }
        }, delay);
    }

    private void updateBtcUsdtTicker() {
        //Every second get the newest info about the coin you want
        mBtcUsdtHandler = new Handler();
        final int delay = 1000; //milliseconds
        mBtcUsdtHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mService.getUSDTBTCPrice();
                mBtcUsdtHandler.postDelayed(this, delay);

            }
        }, delay);
    }

    private void updatePriceTicker() {
        //Every second get the newest info about the coin you want
        mPriceHandler = new Handler();
        final int delay = 1000; //milliseconds

        mPriceHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mService.getLatestPrice();
                mPriceHandler.postDelayed(this, delay);

            }
        }, delay);
    }

    public void startSendTransaction(String quantity, String address) {
        String urlParams = "currency=" + mCoinData.getCurrency() + "&quantity=" + quantity + "&address=" + address;
        String endpoint = "account/withdraw";
        Callback sendCallback = new Callback() {
            Handler mainHandler = new Handler(getMainLooper());

            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            try {
                                String resultJsonString = response.body().string();
                                JSONObject jsonObject = new JSONObject(resultJsonString);
                                if (jsonObject.getString("success").equals("true")) {
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    String uuidStr = result.getString("uuid");
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.message_transaction_successful_with_uuid) + uuidStr, Toast.LENGTH_SHORT).show();
                                } else if (jsonObject.getString("success").equals("false")) {
                                    String messageStr = jsonObject.getString("message");
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed_with_message) + messageStr, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };
        connectBittrexPrivate(endpoint, urlParams, sendCallback);
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

    private void updateDepositAddress() {
        String endpoint = "account/getdepositaddress";
        String urlParams = "currency=" + mCoinData.getCurrency();
        Callback depositCallback = new Callback() {
            Handler mainHandler = new Handler(getMainLooper());

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            try {
                                String resultJsonString = response.body().string();
                                JSONObject jsonObject = new JSONObject(resultJsonString);
                                if (jsonObject.getString("success").equals("true")) {
                                    //Needed to persist across rotation
                                    if (mTransferFragment == null) {
                                        Fragment curFrag = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
                                        //Update it with the new data
                                        if (curFrag instanceof TransferFragment) {
                                            mTransferFragment = (TransferFragment) curFrag;
                                        }
                                    }
                                    if (mTransferFragment != null)
                                        mTransferFragment.updateDepositAddress(resultJsonString);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };

        connectBittrexPrivate(endpoint, urlParams, depositCallback);
    }

    private void connectBittrexPrivate(String endpoint, String urlParameters, Callback callback) {
        String nonce = Long.toString(new Date().getTime());
        Globals globals = (Globals) getApplication();
        String apiKey = globals.getApiKey();
        String apiSecret = globals.getApiSecret();
        String urlString = "https://bittrex.com/api/v1.1/" + endpoint + "?apikey=" + apiKey + "&nonce=" + nonce + "&" + urlParameters;
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .addHeader("apisign", calculateHash(apiSecret, urlString, "HmacSHA512"))
                .build();


        mClient.newCall(request).enqueue(callback);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        startBittrexService();
        super.onResume();
    }

    @Override
    public void onPause() {
        endAllHandlers();
        unbindService(mConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastNewMessage);
        mBound = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Start the service to retrieve market data from Bittrex
     */
    public void startBittrexService() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LiveCoinService, cast the IBinder and get LiveCoinService instance
                LiveBittrexService.LocalBinder binder = (LiveBittrexService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };

        //Bind to the LiveBittrexService
        Globals globals = (Globals) getApplication();
        Intent intent = new Intent(this, LiveBittrexService.class);
        intent.putExtra(API_KEY, globals.getApiKey());
        intent.putExtra(API_SECRET, globals.getApiSecret());
        intent.putExtra(CURRENCY, mCoinData.getCurrency());
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //TODO: Fix the 0.0 displayed to the user as Bittrex is reached for the first time
        //Broadcast the new data to the portfolio fragment
        mBroadCastNewMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Get the fragment from the pager
                Fragment curFrag = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
                //Update it with the new data
                if (curFrag != null) {
                    if (curFrag instanceof OrderHistoryFragment) {
                        mOrderHistoryFragment = (OrderHistoryFragment) curFrag;
                        String closedOrderHistory = intent.getStringExtra(LIVE_CLOSED_ORDER_HISTORY_INTENT_EXTRA);
                        if (closedOrderHistory != null && !closedOrderHistory.isEmpty()) {
                            mOrderHistoryFragment.updateClosedOrderHistory(closedOrderHistory);
                        }
                        String openOpenOrder = intent.getStringExtra(LIVE_OPEN_ORDER_HISTORY_INTENT_EXTRA);
                        if (openOpenOrder != null && !openOpenOrder.isEmpty()) {
                            mOrderHistoryFragment.updateOpenOrderHistory(openOpenOrder);
                        }
                        return;
                    }
                    if (curFrag instanceof OrderBookFragment) {
                        mOrderBookFragment = (OrderBookFragment) curFrag;
                        String orderBook = intent.getStringExtra(LIVE_ORDER_BOOK_INTENT_EXTRA);
                        if (orderBook != null && !orderBook.isEmpty()) {
                            mOrderBookFragment.updateOrderBookHistory(orderBook);
                        }
                        return;
                    }
                    if (curFrag instanceof OrderFragment) {
                        mOrderFragment = (OrderFragment) curFrag;
                        String currencyDetails = intent.getStringExtra(LATEST_PRICE_INTENT_EXTRA);
                        if (currencyDetails != null && !currencyDetails.isEmpty()) {
                            mOrderFragment.updatePrice(currencyDetails);
                        }
                        String btcUSDT = intent.getStringExtra(LATEST_BTC_USDT_PRICE_INTENT_EXTRA);
                        if (btcUSDT != null && !btcUSDT.isEmpty()) {
                            mOrderFragment.updateBTCUSDTPrice(btcUSDT);
                        }

                    }
                    if (curFrag instanceof CoinGraphFragment) {
                        mCoinGraphFragment = (CoinGraphFragment) curFrag;
                        String coinGraph = intent.getStringExtra(LIVE_PRICE_HISTORY_INTENT_EXTRA);
                        if (coinGraph != null && !coinGraph.isEmpty()) {
                            mCoinGraphFragment.updateGraph(coinGraph);
                        }
                        String coinData = intent.getStringExtra(LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_EXTRA);
                        if (coinData != null && !coinData.isEmpty()) {
                            mCoinGraphFragment.updateStats(coinData);
                        }
                        String btcUSDT = intent.getStringExtra(LATEST_BTC_USDT_PRICE_INTENT_EXTRA);
                        if (btcUSDT != null && !btcUSDT.isEmpty()) {
                            mCoinGraphFragment.updateBTCUSDTPrice(btcUSDT);
                        }
                    }
                }
//                }
            }
        };
        IntentFilter bittrexServiceFilter = new IntentFilter();
        //The possible data intent actions
        bittrexServiceFilter.addAction(LIVE_CLOSED_ORDER_HISTORY_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_OPEN_ORDER_HISTORY_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_ORDER_BOOK_INTENT_ACTION);
        bittrexServiceFilter.addAction(LATEST_PRICE_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_PRICE_HISTORY_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_MARKET_DATA_SINGLE_CURRENCY_INTENT_ACTION);
        bittrexServiceFilter.addAction(LATEST_BTC_USDT_PRICE_INTENT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastNewMessage, bittrexServiceFilter);
    }

    /**
     * Get the latest market data by the second
     */
    public void updateOrderHistory() {
        //Every second get the newest info about the coin you want
        mOrderHistoryHandler = new Handler();
        final int delay = 1000; //milliseconds

        mOrderHistoryHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mService.getOpenOrderHistory();
                mService.getClosedOrderHistory();
                mOrderHistoryHandler.postDelayed(this, delay);

            }
        }, delay);
    }

    public void updateOrderBook() {
        //Every second get the newest info about the coin you want
        mOrderBookHandler = new Handler();
        final int delay = 1000; //milliseconds

        mOrderBookHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mService.getOrderBook();
                mOrderBookHandler.postDelayed(this, delay);

            }
        }, delay);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_change_units) {

            Fragment curFrag = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
            //Update it with the new data
            if (curFrag != null) {
                if (curFrag instanceof OrderFragment) {
                    mOrderFragment = (OrderFragment) curFrag;
                    mOrderFragment.changeUnits();
                } else if (curFrag instanceof CoinGraphFragment) {
                    mCoinGraphFragment = (CoinGraphFragment) curFrag;
                    mCoinGraphFragment.changeUnits();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void endAllHandlers() {
        Fragment curFrag = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
        if (mCoinGraph != null && (!((curFrag instanceof CoinGraphFragment) || (curFrag instanceof OrderFragment))))
            mCoinGraph.removeCallbacksAndMessages(null);
        if (mOrderBookHandler != null && !(curFrag instanceof OrderBookFragment))
            mOrderBookHandler.removeCallbacksAndMessages(null);
        if (mOrderHistoryHandler != null && !(curFrag instanceof OrderHistoryFragment))
            mOrderHistoryHandler.removeCallbacksAndMessages(null);
        if (mPriceHandler != null && !(curFrag instanceof OrderFragment))
            mPriceHandler.removeCallbacksAndMessages(null);
        if (mBtcUsdtHandler != null && (!((curFrag instanceof CoinGraphFragment) || (curFrag instanceof OrderFragment))))
            mBtcUsdtHandler.removeCallbacksAndMessages(null);
    }

    public void scanQRCode(View view) {
        //TODO: Remove ZXING library dependency in order to avoid having the user leave the app
//        BarcodeDetector detector =
//                new BarcodeDetector.Builder(getApplicationContext`())
//                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
//                        .build();
//        if(detector.isOperational()){
//            Frame frame
//        }
//        if(!detector.isOperational())
        {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            // handle scan result
            if (mTransferFragment != null) {
                mTransferFragment.updateWalletID(scanResult.getContents());
            }
        }
        // else continue with any other code you need in the method
    }

    public void startBuyTransaction(String quantity, String price) {
        String endpoint = "market/buylimit";
        price = new DecimalFormat("#.########").format(Double.parseDouble(price));
        String urlParams = "market=" + "BTC-" + mCoinData.getCurrency() + "&quantity=" + quantity + "&rate=" + price;
        Callback buyCallback = new Callback() {
            Handler mainHandler = new Handler(getMainLooper());

            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            try {
                                String resultJsonString = response.body().string();
                                JSONObject jsonObject = new JSONObject(resultJsonString);
                                if (jsonObject.getString("success").equals("true")) {
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    String uuidStr = result.getString("uuid");
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.message_transaction_successful_with_uuid) + uuidStr, Toast.LENGTH_SHORT).show();
                                } else if (jsonObject.getString("success").equals("false")) {
                                    String messageStr = jsonObject.getString("message");
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed_with_message) + messageStr, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };

        connectBittrexPrivate(endpoint, urlParams, buyCallback);

    }

    public void startSellTransaction(String quantity, String price) {
        String endpoint = "market/selllimit";
        price = new DecimalFormat("#.########").format(Double.parseDouble(price));
        String urlParams = "market=" + "BTC-" + mCoinData.getCurrency() + "&quantity=" + quantity + "&rate=" + price;
        Callback sellCallback = new Callback() {
            Handler mainHandler = new Handler(getMainLooper());

            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            try {
                                String resultJsonString = response.body().string();
                                JSONObject jsonObject = new JSONObject(resultJsonString);
                                if (jsonObject.getString("success").equals("true")) {
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    String uuidStr = result.getString("uuid");
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.message_transaction_successful_with_uuid) + uuidStr, Toast.LENGTH_SHORT).show();
                                } else if (jsonObject.getString("success").equals("false")) {
                                    String messageStr = jsonObject.getString("message");
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed_with_message) + messageStr, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };

        connectBittrexPrivate(endpoint, urlParams, sellCallback);
    }

    @Override
    public void updateGraphAtInterval(int i) {
        if (mCoinGraph != null) mCoinGraph.removeCallbacksAndMessages(null);
        updateCoinGraph(i);
    }

    @Override
    public void startCoinGraphDataService() {
        endAllHandlers();
        updateCoinGraph(2); //Have to call this method in onCreate since the view pager is
        updateBtcUsdtTicker();
    }

    @Override
    public void onOrderCancelled(OrderHistoryEntry item) {
        if (item != null) {
            String endpoint = "market/cancel";
            String urlParams = "uuid=" + item.getUuid();
            Callback cancelOrderCallback = new Callback() {
                Handler mainHandler = new Handler(getMainLooper());

                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(CoinDataActivity.this, getString(R.string.error_cancel_failed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful()) {
                                try {
                                    String resultJsonString = response.body().string();
                                    JSONObject jsonObject = new JSONObject(resultJsonString);
                                    if (jsonObject.getString("success").equals("true")) {
                                        Toast.makeText(CoinDataActivity.this, getString(R.string.message_order_canceled), Toast.LENGTH_SHORT).show();
                                    } else if (jsonObject.getString("success").equals("false")) {
                                        String messageStr = jsonObject.getString("message");
                                        Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed_with_message) + messageStr, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CoinDataActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            };

            connectBittrexPrivate(endpoint, urlParams, cancelOrderCallback);
        }

    }

    @Override
    public void startOrderBookService() {
        endAllHandlers();
        updateOrderBook();
    }

    @Override
    public void startOrderHistoryService() {
        endAllHandlers();
        updateOrderHistory();
    }

    @Override
    public void startOrderFragmentService() {
        endAllHandlers();
        updatePriceTicker();
        updateBtcUsdtTicker();
    }

    @Override
    public void startTransferFragmentService() {
        endAllHandlers();
        updateDepositAddress();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_coin_data, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private String title[] = {"Price History", "Orders", "Order History", "Book", "Transfer"};


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                if (mCoinGraphFragment == null) {
                    mCoinGraphFragment = CoinGraphFragment.newInstance();
                }
                return mCoinGraphFragment;
            } else if (position == 1) {
                if (mOrderFragment == null) {
                    mOrderFragment = OrderFragment.newInstance();
                }
                return mOrderFragment;
            } else if (position == 2) {
                if (mOrderHistoryFragment == null) {
                    mOrderHistoryFragment = OrderHistoryFragment.newInstance();
                }
                return mOrderHistoryFragment;
            } else if (position == 3) {
                if (mOrderBookFragment == null) {
                    mOrderBookFragment = OrderBookFragment.newInstance();
                }
                return mOrderBookFragment;
            } else if (position == 4) {
                if (mTransferFragment == null) {
                    mTransferFragment = TransferFragment.newInstance();
                }
                return mTransferFragment;
            } else {
                return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}
