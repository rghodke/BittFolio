package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.activity.PortfolioActivity;
import crypto.manager.bittfolio.adapter.CoinRecyclerViewAdapter;
import crypto.manager.bittfolio.model.CoinData;
import crypto.manager.bittfolio.model.PriceData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPortfolioListFragmentInteractionListener}
 * interface.
 */
public class PortfolioFragment extends Fragment {

    private static final String ARG_BALANCES_JSON_STRING = "crypto.manager.bittfolio.ARG_BALANCES_JSON_STRING";
    private static final String TICKER = "TICKER";
    private static final String HOLDING = "HOLDING";
    private static final String PRICE = "PRICE";
    private static final String BALANCE = "BALANCE";

    private String mCoinBalanceString;
    private OnPortfolioListFragmentInteractionListener mListener;
    private List<CoinData> mCoinDataList;
    private CoinRecyclerViewAdapter mRecyclerViewAdapter;
    private TextView mTotalBalanceTextView;
    private ImageView mHappinessIndicator;
    private double mTotalBalance;
    private double mPrevBalance;
    private boolean mIsDollars;
    private TextView m24HourChange;
    private OkHttpClient mClient;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PortfolioFragment() {
    }

    @SuppressWarnings("unused")
    public static PortfolioFragment newInstance(String balanceString) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BALANCES_JSON_STRING, balanceString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mCoinBalanceString = getArguments().getString(ARG_BALANCES_JSON_STRING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio_list, container, false);

        mClient = new OkHttpClient();

        mCoinDataList = new ArrayList<>();
        try {
            mCoinDataList = parseCoinData(mCoinBalanceString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PortfolioActivity portfolioActivity = (PortfolioActivity) getActivity();
        portfolioActivity.setCoinList(mCoinDataList);

        mTotalBalanceTextView = (TextView) view.findViewById(R.id.text_view_portfolio_total_balance);
        mHappinessIndicator = (ImageView) view.findViewById(R.id.image_view_happiness_indicator);
        m24HourChange = (TextView) view.findViewById(R.id.text_view_24_hour_change);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerViewAdapter = new CoinRecyclerViewAdapter(mCoinDataList, mListener);
        recyclerView.setAdapter(mRecyclerViewAdapter);

        return view;
    }

    private List<CoinData> parseCoinData(String coinData) throws JSONException {

        final List<CoinData> coinDataList = new ArrayList<>();

        JSONObject coinBalancesJson = new JSONObject(coinData);

        JSONArray coins = coinBalancesJson.getJSONArray("result");

        for (int i = 0; i < coins.length(); i++) {
            JSONObject coin = coins.getJSONObject(i);
            //Only add coins you posses
            if (coin.getDouble("Balance") != 0.0) {
                CoinData coinObj = new CoinData(coin.getString("Currency"), coin.getDouble("Balance"));
                coinDataList.add(coinObj);
            }
        }

        //Get the icons
        Request request = new Request.Builder()
                .url("https://www.cryptocompare.com/api/data/coinlist/")
                .get()
                .build();


        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String currenciesJSONString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(currenciesJSONString);
                        JSONObject data = jsonObject.getJSONObject("Data");

                        for (CoinData coinData : coinDataList) {
                            //Bittrex has special name for BCH
                            if (coinData.getCurrency().equals("BCC")) {
                                JSONObject currencyData = data.getJSONObject("BCH");
                                coinData.setImageUrl("https://www.cryptocompare.com" + currencyData.getString("ImageUrl"));
                                continue;
                            }
                            if (data.has(coinData.getCurrency())) {
                                JSONObject currencyData = data.getJSONObject(coinData.getCurrency());
                                coinData.setImageUrl("https://www.cryptocompare.com" + currencyData.getString("ImageUrl"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else return;
            }
        });

        return coinDataList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPortfolioListFragmentInteractionListener) {
            mListener = (OnPortfolioListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPortfolioListFragmentInteractionListener");
        }
        mListener.startPortfolioDataService();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Update the coin data with the updated balances
     *
     * @param coinDataString
     */
    public void updateCoinData(String coinDataString) {
        if (coinDataString != null && !coinDataString.isEmpty()) {
            Map<String, PriceData> currencyValue = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(coinDataString);
                JSONArray currenciesData = jsonObject.getJSONArray("result");
                for (int i = 0; i < currenciesData.length(); i++) {
                    JSONObject currencyData = currenciesData.getJSONObject(i);
                    currencyValue.put(currencyData.getString("MarketName"), new PriceData(currencyData.getDouble("Last"), currencyData.getDouble("PrevDay")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mTotalBalance = 0.0;
            mPrevBalance = 0.0;

            for (CoinData coinData : mCoinDataList) {
                String currencyKey = "BTC-" + coinData.getCurrency();
                //btc is always in dollars
                if (coinData.getCurrency().equals("BTC")) {
                    currencyKey = "USDT-" + coinData.getCurrency();
                    double coinValue = currencyValue.get(currencyKey).getLast();
                    double holding = (coinData.getHolding());
                    double balance = coinValue * holding;
                    coinData.setBalance((balance));
                    coinData.setPrice(coinValue);
                    coinData.setPrevDay(currencyValue.get(currencyKey).getPrevDay());
                    //add just the holding to the total since its already in btc units
                    mTotalBalance += holding;
                    mPrevBalance += holding;
                    continue;
                }
                //usdt is always in dollars
                //don't add usdt to total portfolio as bittrex doesn't either
                if (coinData.getCurrency().equals("USDT")) {
                    double coinValue = 1.00;
                    double holding = (coinData.getHolding());
                    double balance = coinValue * holding;
                    coinData.setBalance((balance));
                    coinData.setPrice(coinValue);
                    coinData.setPrevDay(1.00);
                    continue;
                }
                if (currencyValue.containsKey(currencyKey)) {
                    double coinValue = currencyValue.get(currencyKey).getLast();
                    double holding = (coinData.getHolding());
                    double balance = coinValue * holding;
                    //add the btc units worth of the currency
                    mTotalBalance += balance;
                    mPrevBalance += currencyValue.get(currencyKey).getPrevDay() * holding;
                    coinData.setBalance((balance));
                    coinData.setPrice(coinValue);
                    coinData.setPrevDay(currencyValue.get(currencyKey).getPrevDay());
                }
            }

            if (mIsDollars) {
                String USDBTC = "USDT-BTC";
                double coinValue = currencyValue.get(USDBTC).getLast();
                for (CoinData coinData : mCoinDataList) {
                    //btc is always in dollars
                    if (coinData.getCurrency().equals("BTC")) {
                        continue;
                    }
                    if (coinData.getCurrency().equals("USDT")) {
                        continue;
                    }
                    coinData.setBalance(coinData.getBalance() * coinValue);
                    coinData.setPrice(coinData.getPrice() * coinValue);
                    coinData.setPrevDay(coinData.getPrevDay() * coinValue);
                }
                mTotalBalance *= coinValue;
                mPrevBalance *= currencyValue.get(USDBTC).getPrevDay();
            }

            //Update the list with the second to second update on balances
            refreshPortfolioData(mTotalBalance);

        }

    }

    private void refreshPortfolioData(double totalBalance) {
        String currency = mIsDollars ? "$" : "₿";
        if (mIsDollars) {
            String val = currency + new DecimalFormat("#.00").format(totalBalance);
            mTotalBalanceTextView.setText(val);
        } else {
            String val = currency + new DecimalFormat("#.#######").format(totalBalance);
            mTotalBalanceTextView.setText(val);
        }

        //update recyclerview
        mRecyclerViewAdapter.notifyDataSetChanged();
        mRecyclerViewAdapter.setTotalBalance(totalBalance);

        //24 hour % change
        double percentChange = ((mTotalBalance / mPrevBalance) - 1) * 100;
        String percentChangeStr = new DecimalFormat("#.00").format(percentChange) + "%";
        m24HourChange.setText(percentChangeStr);

        //Negative change in portfolio balance
        if (totalBalance < mPrevBalance) {
            Picasso.with(getContext()).load(R.drawable.sad_face).fit().into(mHappinessIndicator);
        } else if (totalBalance >= mPrevBalance) { //Positive change in portfolio
            Picasso.with(getContext()).load(R.drawable.happy_face).fit().into(mHappinessIndicator);
        }
    }

    public void changeUnits() {
        mIsDollars = !mIsDollars;
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.setDollars(mIsDollars);
        }
    }

    public void sortBy(String sortingMethod) {
        if (sortingMethod.equals(TICKER)) {
            Collections.sort(mCoinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return coinData.getCurrency().compareTo(t1.getCurrency());
                }
            });
        }

        if (sortingMethod.equals(HOLDING)) {
            Collections.sort(mCoinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return Double.compare(t1.getHolding(), coinData.getHolding());
                }
            });
        }

        if (sortingMethod.equals(BALANCE)) {
            Collections.sort(mCoinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return Double.compare(t1.getBalance(), coinData.getBalance());
                }
            });
        }

        if (sortingMethod.equals(PRICE)) {
            Collections.sort(mCoinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return Double.compare(t1.getPrice(), coinData.getPrice());
                }
            });
        }

        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void showHideBalance(boolean isPercent) {
        mRecyclerViewAdapter.changeBalanceToPercent(isPercent);
    }

    public void showHideHoldings(boolean isHidden) {
        mRecyclerViewAdapter.changeHoldingVisibility(isHidden);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPortfolioListFragmentInteractionListener {
        // TODO: Update argument type and name
        void startPortfolioDataService();

        void onCoinSelected(CoinData item);
    }

}
