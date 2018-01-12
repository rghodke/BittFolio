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
import android.support.v4.app.FragmentActivity;
import android.view.View;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.service.LiveBittrexService;

public class PortfolioActivity extends FragmentActivity implements PortfolioFragment.OnPortfolioListFragmentInteractionListener {

    private static final String ARG_COIN_DATA = "COIN_DATA";
    private static final String TAG_PORTFOLIO_FRAGMENT = "PORTFOLIO_FRAGMENT";
    private static final String EXTRA_COIN_BALANCE_STRING = "EXTRA_COIN_BALANCE_STRING";
    private static final String TAG_COIN_DATA_FRAGMENT = "COIN_DATA_FRAGMENT";
    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String TICKER = "TICKER";
    private static final String HOLDING = "HOLDING";
    private static final String PRICE = "PRICE";
    private static final String BALANCE = "BALANCE";
    private final String LIVE_COIN_INTENT_EXTRA = "LIVE_COIN_INTENT_EXTRA";
    private final String LIVE_COIN_INTENT_ACTION = "LIVE_COIN_INTENT_ACTION";
    private PortfolioFragment mPortfolioFragment;
    private LiveBittrexService mService;
    private ServiceConnection mConnection;
    private boolean mBound = false;
    private BroadcastReceiver mBroadCastNewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        String coinBalanceString = "";
        if (getIntent().hasExtra(EXTRA_COIN_BALANCE_STRING)) {
            coinBalanceString = getIntent().getStringExtra(EXTRA_COIN_BALANCE_STRING);
        }

        //persist the fragment through rotation
        if (savedInstanceState != null) {
            mPortfolioFragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(TAG_PORTFOLIO_FRAGMENT);
        } else {
            mPortfolioFragment = PortfolioFragment.newInstance(coinBalanceString);
        }


        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mPortfolioFragment, TAG_PORTFOLIO_FRAGMENT).commit();

    }

    @Override
    public void onPortfolioListFragmentInteraction(CoinData item) {
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
                if (mPortfolioFragment != null) {
                    mPortfolioFragment.updateCoinData(intent.getStringExtra(LIVE_COIN_INTENT_EXTRA));
                }
            }
        };
        registerReceiver(mBroadCastNewMessage, new IntentFilter(LIVE_COIN_INTENT_ACTION));

        //Update the price on a second basis
        updateCoinPrice();
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
        unregisterReceiver(mBroadCastNewMessage);
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
        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mService.getLiveCoinValues();
                handler.postDelayed(this, delay);

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
}
