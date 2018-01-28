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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.CoinSearchFragment;
import crypto.manager.bittfolio.fragment.OverallOrderHistoryFragment;
import crypto.manager.bittfolio.fragment.PortfolioFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.model.OrderHistoryEntry;
import crypto.manager.bittfolio.service.LiveBittrexService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortfolioActivity extends AppCompatActivity implements PortfolioFragment.OnPortfolioListFragmentInteractionListener, CoinSearchFragment.onCoinSearchFragmentInteractionListener, OverallOrderHistoryFragment.OnOverallOrderHistoryListFragmentInteractionListener {

    private static final String ARG_COIN_DATA = "crypto.manager.bittfolio.ARG_COIN_DATA";
    private static final String TAG_PORTFOLIO_FRAGMENT = "TAG_PORTFOLIO_FRAGMENT";
    private static final String TAG_OVERALL_ORDER_HISTORY_FRAGMENT = "TAG_OVERALL_ORDER_HISTORY_FRAGMENT";
    private static final String TAG_COIN_SEARCH_FRAGMENT = "TAG_COIN_SEARCH_FRAGMENT";
    private static final String EXTRA_COIN_BALANCE_STRING = "crypto.manager.bittfolio.EXTRA_COIN_BALANCE_STRING";
    private static final String API_KEY = "crypto.manager.bittfolio.API_KEY";
    private static final String API_SECRET = "crypto.manager.bittfolio.API_SECRET";
    private static final String TICKER = "TICKER";
    private static final String HOLDING = "HOLDING";
    private static final String PRICE = "PRICE";
    private static final String BALANCE = "BALANCE";
    private static final String LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_EXTRA";
    private static final String LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_ACTION";
    private static final String LIVE_COIN_INTENT_EXTRA = "crypto.manager.bittfolio.LIVE_COIN_INTENT_EXTRA";
    private static final String LIVE_COIN_INTENT_ACTION = "crypto.manager.bittfolio.LIVE_COIN_INTENT_ACTION";
    private static final String CURRENT_FRAGMENT_INTENT_EXTRA = "crypto.manager.bittfolio.CURRENT_FRAGMENT_INTENT_EXTRA";
    private static PortfolioFragment mPortfolioFragment;
    private static CoinSearchFragment mCoinSearchFragment;
    private static OverallOrderHistoryFragment mOverallOrderHistoryFragment;
    private LiveBittrexService mService;
    private ServiceConnection mConnection;
    private boolean mBound = false;
    private BroadcastReceiver mBroadCastNewMessage;
    private boolean mIsPercent;
    private boolean mIsHoldingHidden;
    private List<CoinData> mCoinList;
    private OkHttpClient mClient;
    private Handler mOverallOrderHistoryHandler;
    private Handler mCoinPriceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        mClient = new OkHttpClient();

        String coinBalanceString = "";
        if (getIntent().hasExtra(EXTRA_COIN_BALANCE_STRING)) {
            coinBalanceString = getIntent().getStringExtra(EXTRA_COIN_BALANCE_STRING);
        }


        String fragmentToDisplay;

        //persist the fragment through rotation
        if (savedInstanceState != null) {
            mPortfolioFragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(TAG_PORTFOLIO_FRAGMENT);
            mCoinSearchFragment = (CoinSearchFragment) getSupportFragmentManager().findFragmentByTag(TAG_COIN_SEARCH_FRAGMENT);
            mOverallOrderHistoryFragment = (OverallOrderHistoryFragment) getSupportFragmentManager().findFragmentByTag(TAG_OVERALL_ORDER_HISTORY_FRAGMENT);
            fragmentToDisplay = savedInstanceState.getString(CURRENT_FRAGMENT_INTENT_EXTRA);
        } else {
            mPortfolioFragment = PortfolioFragment.newInstance(coinBalanceString);
            mCoinSearchFragment = CoinSearchFragment.newInstance();
            mOverallOrderHistoryFragment = OverallOrderHistoryFragment.newInstance();
            fragmentToDisplay = TAG_PORTFOLIO_FRAGMENT;
        }

        if (fragmentToDisplay.equals(TAG_COIN_SEARCH_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mCoinSearchFragment, TAG_COIN_SEARCH_FRAGMENT).commit();
        } else if (fragmentToDisplay.equals(TAG_OVERALL_ORDER_HISTORY_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mOverallOrderHistoryFragment, TAG_OVERALL_ORDER_HISTORY_FRAGMENT).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mPortfolioFragment, TAG_PORTFOLIO_FRAGMENT).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_portfolio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        String currentFragDisplayed = "";

        Fragment curFrag = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (curFrag instanceof PortfolioFragment) {
            currentFragDisplayed = TAG_PORTFOLIO_FRAGMENT;
        } else if (curFrag instanceof OverallOrderHistoryFragment) {
            currentFragDisplayed = TAG_OVERALL_ORDER_HISTORY_FRAGMENT;
        } else if (curFrag instanceof CoinSearchFragment) {
            currentFragDisplayed = TAG_COIN_SEARCH_FRAGMENT;
        } else {
            currentFragDisplayed = TAG_PORTFOLIO_FRAGMENT;
        }
        bundle.putString(CURRENT_FRAGMENT_INTENT_EXTRA, currentFragDisplayed);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            if (mCoinSearchFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mCoinSearchFragment, TAG_COIN_SEARCH_FRAGMENT).addToBackStack(null).commit();
            } else {
                mCoinSearchFragment = CoinSearchFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mCoinSearchFragment, TAG_COIN_SEARCH_FRAGMENT).addToBackStack(null).commit();
            }
        }
        if (id == R.id.action_order_history) {
            if (mOverallOrderHistoryFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mOverallOrderHistoryFragment, TAG_OVERALL_ORDER_HISTORY_FRAGMENT).addToBackStack(null).commit();
            } else {
                mOverallOrderHistoryFragment = OverallOrderHistoryFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mOverallOrderHistoryFragment, TAG_OVERALL_ORDER_HISTORY_FRAGMENT).addToBackStack(null).commit();
            }
        }
        if (id == R.id.action_change_balance_percent) {
            //Update the list
            if (mPortfolioFragment != null) {
                mIsPercent = !mIsPercent;
                if (mIsPercent) {
                    item.setTitle(R.string.label_hide_percent_balance);
                } else {
                    item.setTitle(R.string.label_show_percent_balance);
                }
                mPortfolioFragment.showHideBalance(mIsPercent);
            }
        }

        if (id == R.id.action_hide_holdings) {
            //Update the list
            if (mPortfolioFragment != null) {
                mIsHoldingHidden = !mIsHoldingHidden;
                if (mIsHoldingHidden) {
                    item.setTitle(R.string.label_show_holdings);
                } else {
                    item.setTitle(R.string.label_hide_holdings);
                }
                mPortfolioFragment.showHideHoldings(mIsHoldingHidden);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startPortfolioDataService() {
        stopAllHandlers();
        //Update the price on a second basis
        updateCoinPrice();
    }

    private void stopAllHandlers() {
        if (mCoinPriceHandler != null) mCoinPriceHandler.removeCallbacksAndMessages(null);
        if (mOverallOrderHistoryHandler != null)
            mOverallOrderHistoryHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCoinSelected(CoinData item) {
        Intent intent = new Intent(this, CoinDataActivity.class);
        intent.putExtra(ARG_COIN_DATA, item);
        startActivity(intent);
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
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //TODO: Fix the 0.0 displayed to the user as Bittrex is reached for the first time
        //Broadcast the new data to the portfolio fragment
        mBroadCastNewMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Fragment curFrag = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
                if (curFrag instanceof PortfolioFragment) {
                    mPortfolioFragment = (PortfolioFragment) curFrag;
                    mPortfolioFragment.updateCoinData(intent.getStringExtra(LIVE_COIN_INTENT_EXTRA));
                    return;
                } else if (curFrag instanceof OverallOrderHistoryFragment) {
                    mOverallOrderHistoryFragment = (OverallOrderHistoryFragment) curFrag;
                    String closedOrderHistory = intent.getStringExtra(LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_EXTRA);
                    if (closedOrderHistory != null && !closedOrderHistory.isEmpty()) {
                        mOverallOrderHistoryFragment.updateClosedOrderHistory(closedOrderHistory);
                    }
                    String openOpenOrder = intent.getStringExtra(LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_EXTRA);
                    if (openOpenOrder != null && !openOpenOrder.isEmpty()) {
                        mOverallOrderHistoryFragment.updateOpenOrderHistory(openOpenOrder);
                    }
                    return;
                }
            }
        };
        IntentFilter bittrexServiceFilter = new IntentFilter();
        bittrexServiceFilter.addAction(LIVE_COIN_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_OVERALL_CLOSED_ORDER_HISTORY_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_OVERALL_OPEN_ORDER_HISTORY_INTENT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastNewMessage, bittrexServiceFilter);

    }

    private void updateOverallOrderHistory() {
        //Every second get the newest info about the coin you want
        mOverallOrderHistoryHandler = new Handler();
        final int delay = 1000; //milliseconds

        mOverallOrderHistoryHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                if (mService != null) mService.getOverallClosedOrderHistory();
                if (mService != null) mService.getOverallOpenOrderHistory();
                mOverallOrderHistoryHandler.postDelayed(this, delay);

            }
        }, delay);
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
     * Get the latest market data by the second
     */
    public void updateCoinPrice() {
        //Every second get the newest info about the coin you want
        mCoinPriceHandler = new Handler();
        final int delay = 1000; //milliseconds

        mCoinPriceHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                if (mService != null) mService.getLiveCoinValues();
                mCoinPriceHandler.postDelayed(this, delay);

            }
        }, delay);
    }

    public void changeUnit(View view) {
        if (mPortfolioFragment != null) {
            mPortfolioFragment.changeUnits();
        }
    }

    public void sortByTicker(View view) {
        if (mPortfolioFragment != null)
            mPortfolioFragment.sortBy(TICKER);
    }

    public void sortByHolding(View view) {
        if (mPortfolioFragment != null)
            mPortfolioFragment.sortBy(HOLDING);
    }

    public void sortByPrice(View view) {
        if (mPortfolioFragment != null)
            mPortfolioFragment.sortBy(PRICE);
    }

    public void sortByBalance(View view) {
        if (mPortfolioFragment != null)
            mPortfolioFragment.sortBy(BALANCE);
    }

    @Override
    public void onCoinSelected(String coin) {
        if (mCoinList != null) {
            for (CoinData coinData : mCoinList) {
                if (coinData.getCurrency().equals(coin)) {
                    onCoinSelected(coinData);
                    return;
                }
            }
        }
        onCoinSelected(new CoinData(coin, 0.0));
    }

    public void setCoinList(List<CoinData> mCoinList) {
        this.mCoinList = mCoinList;
    }

    @Override
    public void startOverallOrderHistoryDataService() {
        stopAllHandlers();
        //Update overall order history
        updateOverallOrderHistory();
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
                    Toast.makeText(PortfolioActivity.this, getString(R.string.error_cancel_failed), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(PortfolioActivity.this, getString(R.string.message_order_canceled), Toast.LENGTH_SHORT).show();
                                    } else if (jsonObject.getString("success").equals("false")) {
                                        String messageStr = jsonObject.getString("message");
                                        Toast.makeText(PortfolioActivity.this, getString(R.string.error_transaction_failed_with_message) + messageStr, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PortfolioActivity.this, getString(R.string.error_transaction_failed), Toast.LENGTH_SHORT).show();
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
}
