package crypto.manager.bittfolio.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import crypto.manager.bittfolio.Globals;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.OrderHistoryFragment;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.service.LiveOrderHistoryService;

public class CoinDataActivity extends AppCompatActivity {

    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String CURRENCY = "CURRENCY";
    private static final String ARG_COIN_DATA = "COIN_DATA";
    private static final String TAG_ORDER_HISTORY_DATA_FRAGMENT = "ORDER_HISTORY_FRAGMENT";
    private final String LIVE_ORDER_HISTORY_EXTRA = "LIVE_ORDER_HISTORY_EXTRA";
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
    private LiveOrderHistoryService mService;
    private ServiceConnection mConnection;
    private boolean mBound = false;
    private BroadcastReceiver mBroadCastNewMessage;
    private OrderHistoryFragment mOrderHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_data);

        mCoinData = getIntent().getExtras().getParcelable(ARG_COIN_DATA);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mCoinData.getCurrency());
        setSupportActionBar(toolbar);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        startLiveOrderHistoryService();
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
     * Start the service to retrieve market data from Bittrex
     */
    public void startLiveOrderHistoryService() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LiveCoinService, cast the IBinder and get LiveCoinService instance
                LiveOrderHistoryService.LocalBinder binder = (LiveOrderHistoryService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };

        //Bind to the LiveCoinValueService
        Globals globals = (Globals) getApplication();
        Intent intent = new Intent(this, LiveOrderHistoryService.class);
        intent.putExtra(API_KEY, globals.getApiKey());
        intent.putExtra(API_SECRET, globals.getApiSecret());
        intent.putExtra(CURRENCY, mCoinData.getCurrency());
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //TODO: Fix the 0.0 displayed to the user as Bittrex is reached for the first time
        //Broadcast the new data to the portfolio fragment
        mBroadCastNewMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mViewPagerCurrentFragment = mViewPager.getCurrentItem();
                if (mViewPagerCurrentFragment == 1) {
                    //Get the fragment from the pager
                    if (mOrderHistoryFragment == null) {
                        mOrderHistoryFragment = (OrderHistoryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
                    }
                    //Update it with the new data
                    if (mOrderHistoryFragment != null)
                        mOrderHistoryFragment.updateOrderHistory(intent.getStringExtra(LIVE_ORDER_HISTORY_EXTRA));
                }
            }
        };
        registerReceiver(mBroadCastNewMessage, new IntentFilter(LIVE_ORDER_HISTORY_INTENT_ACTION));

        //Update the price on a second basis
        updateOrderHistory();
    }

    /**
     * Get the latest market data by the second
     */
    public void updateOrderHistory() {
        //Every second get the newest info about the coin you want
        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                mService.getOrderHistory();
                handler.postDelayed(this, delay);

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
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private String title[] = {"Orders", "Order History", "Book", "Transfer"};


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position != 1) return PlaceholderFragment.newInstance(position + 1);
            else {
                if (mOrderHistoryFragment == null) {
                    mOrderHistoryFragment = OrderHistoryFragment.newInstance();
                }
                return mOrderHistoryFragment;
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
