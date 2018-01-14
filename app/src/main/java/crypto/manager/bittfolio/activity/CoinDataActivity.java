package crypto.manager.bittfolio.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.OrderBookFragment;
import crypto.manager.bittfolio.fragment.OrderFragment;
import crypto.manager.bittfolio.fragment.OrderHistoryFragment;
import crypto.manager.bittfolio.fragment.TransferFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.service.LiveBittrexService;

public class CoinDataActivity extends AppCompatActivity {

    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String CURRENCY = "CURRENCY";
    private static final String ARG_COIN_DATA = "COIN_DATA";
    private static final String LIVE_ORDER_BOOK_INTENT_EXTRA = "LIVE_ORDER_BOOK_INTENT_EXTRA";
    private static final String LIVE_ORDER_BOOK_INTENT_ACTION = "LIVE_ORDER_BOOK_INTENT_ACTION";
    private static final String LATEST_PRICE_INTENT_ACTION = "LATEST_PRICE_INTENT_ACTION";
    private static final String LATEST_PRICE_INTENT_EXTRA = "LATEST_PRICE_INTENT_EXTRA";
    private final String LIVE_ORDER_HISTORY_INTENT_EXTRA = "LIVE_ORDER_HISTORY_INTENT_EXTRA";
    private final String LIVE_ORDER_HISTORY_INTENT_ACTION = "LIVE_ORDER_HISTORY_INTENT_ACTION";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_data);

        mCoinData = getIntent().getExtras().getParcelable(ARG_COIN_DATA);

        getSupportActionBar().setTitle(mCoinData.getCurrency());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Start the appropriate handler for the appropriate fragment
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                endAllHandlers();
                if (position == 0) {
                    updatePriceTicker();
                } else if (position == 1) {
                    updateOrderHistory();
                } else if (position == 2) {
                    updateOrderBook();
                } else if (position == 3) {
                    updateDepositAddress();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Set up the different tabs
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        updatePriceTicker(); //Have to call this method in onCreate since the view pager is
        // not triggered until user interacts
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
        Globals globals = (Globals) getApplication();
        new SendTransaction(globals.getApiKey(), globals.getApiSecret(), quantity, address).execute();
    }

    private void updateDepositAddress() {
        Globals globals = (Globals) getApplication();
        new GetDepositTask(globals.getApiKey(), globals.getApiSecret()).execute();
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
        unregisterReceiver(mBroadCastNewMessage);
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
                        String orderHistory = intent.getStringExtra(LIVE_ORDER_HISTORY_INTENT_EXTRA);
                        if (orderHistory != null && !orderHistory.isEmpty()) {
                            mOrderHistoryFragment.updateOrderHistory(orderHistory);
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
                    }
                }
//                }
            }
        };
        IntentFilter bittrexServiceFilter = new IntentFilter();
        //The possible data intent actions
        bittrexServiceFilter.addAction(LIVE_ORDER_HISTORY_INTENT_ACTION);
        bittrexServiceFilter.addAction(LIVE_ORDER_BOOK_INTENT_ACTION);
        bittrexServiceFilter.addAction(LATEST_PRICE_INTENT_ACTION);
        registerReceiver(mBroadCastNewMessage, bittrexServiceFilter);
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
                mService.getOrderHistory();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_coin_data, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void endAllHandlers() {
        if (mOrderBookHandler != null) mOrderBookHandler.removeCallbacksAndMessages(null);
        if (mOrderHistoryHandler != null) mOrderHistoryHandler.removeCallbacksAndMessages(null);
        if (mPriceHandler != null) mPriceHandler.removeCallbacksAndMessages(null);
    }

    public void scanQRCode(View view) {
        //TODO: Remove ZXING library dependency in order to avoid having the user leave the app
//        BarcodeDetector detector =
//                new BarcodeDetector.Builder(getApplicationContext())
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
        Globals globals = (Globals) getApplication();
        new LimitBuyTask(globals.getApiKey(), globals.getApiSecret(), "BTC-" + mCoinData.getCurrency(), quantity, price).execute();
    }

    public void startSellTransaction(String quantity, String price) {
        Globals globals = (Globals) getApplication();
        new LimitSellTask(globals.getApiKey(), globals.getApiSecret(), "BTC-" + mCoinData.getCurrency(), quantity, price).execute();
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
        private String title[] = {"Orders", "Order History", "Book", "Transfer"};


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                if (mOrderFragment == null) {
                    mOrderFragment = OrderFragment.newInstance();
                }
                return mOrderFragment;
            } else if (position == 1) {
                if (mOrderHistoryFragment == null) {
                    mOrderHistoryFragment = OrderHistoryFragment.newInstance();
                }
                return mOrderHistoryFragment;
            } else if (position == 2) {
                if (mOrderBookFragment == null) {
                    mOrderBookFragment = OrderBookFragment.newInstance();
                }
                return mOrderBookFragment;
            } else if (position == 3) {
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

    /**
     * Represents an asynchronous task used to retrieve deposit info
     */
    public class GetDepositTask extends AsyncTask<Void, Void, Boolean> {


        private final String mApiKey;
        private final String mApiSecret;
        private String mResult;

        public GetDepositTask(String apiKey, String apiSecret) {
            mApiKey = apiKey;
            mApiSecret = apiSecret;
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
                String urlString = "https://bittrex.com/api/v1.1/account/getdepositaddress?apikey=" + mApiKey + "&nonce=" + nonce + "&currency=" + mCoinData.getCurrency();
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
                    mResult = resultBuffer.toString();
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
            if (success) {

                //Needed to persist across rotation
                if (mTransferFragment == null) {
                    Fragment curFrag = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
                    //Update it with the new data
                    if (curFrag instanceof TransferFragment) {
                        mTransferFragment = (TransferFragment) curFrag;
                    }
                }
                if (mTransferFragment != null) mTransferFragment.updateDepositAddress(mResult);

            } else {
            }
        }
    }


    /**
     * Represents an asynchronous task used to make a buy limit order
     */
    public class LimitBuyTask extends AsyncTask<Void, Void, Boolean> {


        private final String mApiKey;
        private final String mApiSecret;
        private final String mQuantity;
        private final String mPrice;
        private String mResult;
        private String mCurrency;

        public LimitBuyTask(String apiKey, String apiSecret, String currency, String quantity, String price) {
            mApiKey = apiKey;
            mApiSecret = apiSecret;
            mCurrency = currency;
            mQuantity = quantity;
            mPrice = price;
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
                //TODO: Remove typo from buy order logic. It is in place to prevent any accidental buys
                String urlString = "https://bittrex.com/api/v1.1/market/_REMOVETHISTYPO_buylimit?apikey=" + mApiKey + "&nonce=" + nonce + "&market=" + mCurrency + "&quantity=" + mQuantity + "&rate=" + mPrice;
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
                        JSONObject result = coinBalancesJson.getJSONObject("result");
                        mResult = result.getString("uuid");
                    } catch (JSONException e) {
                        success = "false";
                    }
                    if (success.equals("false")) {
                        return false;
                    }
                    mResult = resultBuffer.toString();
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
            if (success) {
                Toast.makeText(CoinDataActivity.this, "Transaction Successful with UUID " + mResult, Toast.LENGTH_SHORT).show();
            } else {
            }
        }
    }

    /**
     * Represents an asynchronous task used to make a sell limit order
     */
    public class LimitSellTask extends AsyncTask<Void, Void, Boolean> {


        private final String mApiKey;
        private final String mApiSecret;
        private final String mQuantity;
        private final String mPrice;
        private String mResult;
        private String mCurrency;

        public LimitSellTask(String apiKey, String apiSecret, String currency, String quantity, String price) {
            mApiKey = apiKey;
            mApiSecret = apiSecret;
            mCurrency = currency;
            mQuantity = quantity;
            mPrice = price;
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
                //TODO: Remove typo from sell order logic. It is in place to prevent any accidental buys
                String urlString = "https://bittrex.com/api/v1.1/market/_REMOVETHISTYPO_selllimit?apikey=" + mApiKey + "&nonce=" + nonce + "&market=" + mCurrency + "&quantity=" + mQuantity + "&rate=" + mPrice;
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
                        JSONObject result = coinBalancesJson.getJSONObject("result");
                        mResult = result.getString("uuid");
                    } catch (JSONException e) {
                        success = "false";
                    }
                    if (success.equals("false")) {
                        return false;
                    }
                    mResult = resultBuffer.toString();
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
            if (success) {
                Toast.makeText(CoinDataActivity.this, "Transaction Successful with UUID " + mResult, Toast.LENGTH_SHORT).show();
            } else {
            }
        }
    }


    /**
     * Represents an asynchronous task used to send currency to another wallet
     */
    public class SendTransaction extends AsyncTask<Void, Void, Boolean> {


        private final String mApiKey;
        private final String mApiSecret;
        private final String mQuantity;
        private final String mAddress;
        private String mResult;

        public SendTransaction(String apiKey, String apiSecret, String quantity, String address) {
            mApiKey = apiKey;
            mApiSecret = apiSecret;
            mQuantity = quantity;
            mAddress = address;
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
                //TODO: Remove typo from withdraw. In place to avoid commiting any transactions
                String urlString = "https://bittrex.com/api/v1.1/account/_REMOVETHISTYPO_withdraw?apikey=" + mApiKey + "&nonce=" + nonce + "&currency=" + mCoinData.getCurrency() + "&quantity=" + mQuantity + "&address=" + mAddress;
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
                        JSONObject result = coinBalancesJson.getJSONObject("result");
                        mResult = result.getString("uuid");
                    } catch (JSONException e) {
                        success = "false";
                    }
                    if (success.equals("false")) {
                        return false;
                    }
                    mResult = resultBuffer.toString();
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
            if (success) {
                Toast.makeText(CoinDataActivity.this, "Transaction Successful with UUID " + mResult, Toast.LENGTH_SHORT).show();
            } else {
            }
        }
    }
}
