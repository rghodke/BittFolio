package crypto.manager.bittfolio.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;


import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.service.LiveCoinValueService;

public class PortfolioActivity extends FragmentActivity implements PortfolioFragment.OnPortfolioListFragmentInteractionListener {

    private static final String ARG_COIN_DATA = "COIN_DATA";
    private PortfolioFragment mPortfolioFragment;
    private static final String TAG_PORTFOLIO_FRAGMENT = "PORTFOLIO_FRAGMENT";
    private static final String EXTRA_COIN_BALANCE_STRING = "EXTRA_COIN_BALANCE_STRING";
    private final String LIVE_COIN_INTENT_EXTRA = "COIN_LAST_VALUES";
    private final String LIVE_COIN_INTENT_ACTION = "COIN_RETRIEVE_ACTION";
    private static final String TAG_COIN_DATA_FRAGMENT = "COIN_DATA_FRAGMENT";
    private LiveCoinValueService mService;
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
    public void startLiveCoinValueService() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LiveCoinService, cast the IBinder and get LiveCoinService instance
                LiveCoinValueService.LocalBinder binder = (LiveCoinValueService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };

        //Bind to the LiveCoinValueService
        Intent intent = new Intent(this, LiveCoinValueService.class);;
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
    public void onResume(){
        startLiveCoinValueService();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        unregisterReceiver(mBroadCastNewMessage);
        mBound = false;
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
}
