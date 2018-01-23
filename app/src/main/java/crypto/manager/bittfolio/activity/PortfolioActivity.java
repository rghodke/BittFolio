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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.CoinSearchFragment;
import crypto.manager.bittfolio.fragment.PortfolioFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.service.LiveBittrexService;

public class PortfolioActivity extends AppCompatActivity implements PortfolioFragment.OnPortfolioListFragmentInteractionListener, CoinSearchFragment.onCoinSearchFragmentInteractionListener {

    private static final String ARG_COIN_DATA = "ARG_COIN_DATA";
    private static final String TAG_PORTFOLIO_FRAGMENT = "TAG_PORTFOLIO_FRAGMENT";
    private static final String TAG_COIN_SEARCH_FRAGMENT = "TAG_COIN_SEARCH_FRAGMENT";
    private static final String EXTRA_COIN_BALANCE_STRING = "EXTRA_COIN_BALANCE_STRING";
    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String TICKER = "TICKER";
    private static final String HOLDING = "HOLDING";
    private static final String PRICE = "PRICE";
    private static final String BALANCE = "BALANCE";
    private static final String LIVE_COIN_INTENT_EXTRA = "LIVE_COIN_INTENT_EXTRA";
    private static final String LIVE_COIN_INTENT_ACTION = "LIVE_COIN_INTENT_ACTION";
    private static final String CURRENT_FRAGMENT_INTENT_EXTRA = "CURRENT_FRAGMENT_INTENT_EXTRA";
    private static PortfolioFragment mPortfolioFragment;
    private static CoinSearchFragment mCoinSearchFragment;
    private LiveBittrexService mService;
    private ServiceConnection mConnection;
    private boolean mBound = false;
    private BroadcastReceiver mBroadCastNewMessage;
    private boolean mIsPercent;
    private boolean mIsHoldingHidden;
    private List<CoinData> mCoinList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        String coinBalanceString = "";
        if (getIntent().hasExtra(EXTRA_COIN_BALANCE_STRING)) {
            coinBalanceString = getIntent().getStringExtra(EXTRA_COIN_BALANCE_STRING);
        }

        String fragmentToDisplay;

        //persist the fragment through rotation
        if (savedInstanceState != null) {
            mPortfolioFragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(TAG_PORTFOLIO_FRAGMENT);
            mCoinSearchFragment = (CoinSearchFragment) getSupportFragmentManager().findFragmentByTag(TAG_COIN_SEARCH_FRAGMENT);
            fragmentToDisplay = savedInstanceState.getString(CURRENT_FRAGMENT_INTENT_EXTRA);
        } else {
            mPortfolioFragment = PortfolioFragment.newInstance(coinBalanceString);
            mCoinSearchFragment = CoinSearchFragment.newInstance();
            fragmentToDisplay = TAG_PORTFOLIO_FRAGMENT;
        }

        if (fragmentToDisplay.equals(TAG_COIN_SEARCH_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mCoinSearchFragment, TAG_COIN_SEARCH_FRAGMENT).commit();
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
        if (mCoinSearchFragment != null) {
            if (mCoinSearchFragment.isVisible()) {
                currentFragDisplayed = TAG_COIN_SEARCH_FRAGMENT;
            }
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
                if (mService != null) mService.getLiveCoinValues();
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

    @Override
    public void onCoinSelected(String coin) {
        if (mCoinList != null) {
            for (CoinData coinData : mCoinList) {
                if (coinData.getCurrency().equals(coin)) {
                    onPortfolioListFragmentInteraction(coinData);
                    return;
                }
            }
        }
        onPortfolioListFragmentInteraction(new CoinData(coin, 0.0));
    }

    public void setCoinList(List<CoinData> mCoinList) {
        this.mCoinList = mCoinList;
    }
}
