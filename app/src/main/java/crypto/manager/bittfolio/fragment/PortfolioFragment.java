package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.CoinRecyclerViewAdapter;
import crypto.manager.bittfolio.model.CoinData;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPortfolioListFragmentInteractionListener}
 * interface.
 */
public class PortfolioFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_BALANCES_JSON_STRING = "COIN_BALANCES";
    private static final String TICKER = "TICKER";
    private static final String HOLDING = "HOLDING";
    private static final String PRICE = "PRICE";
    private static final String BALANCE = "BALANCE";
    // TODO: Customize parameters
    private String mCoinBalanceString;
    private OnPortfolioListFragmentInteractionListener mListener;
    private List<CoinData> coinDataList;
    private CoinRecyclerViewAdapter recyclerViewAdapter;
    private TextView mTotalBalance;
    private ImageView mHappinessIndicator;
    private double totalBalance;
    private double prevBalance = 0.0;
    private boolean isDollars;

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

        coinDataList = new ArrayList<>();
        try {
            coinDataList = parseCoinData(mCoinBalanceString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mTotalBalance = (TextView) view.findViewById(R.id.text_view_portfolio_total_balance);
        mHappinessIndicator = (ImageView) view.findViewById(R.id.image_view_happiness_indicator);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerViewAdapter = new CoinRecyclerViewAdapter(coinDataList, mListener);
        recyclerView.setAdapter(recyclerViewAdapter);

        return view;
    }

    private List<CoinData> parseCoinData(String coinData) throws JSONException {

        List<CoinData> coinDataList = new ArrayList<>();

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

        new RetrieveIconImage(coinDataList).execute();

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
        Map<String, Double> currencyValue = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(coinDataString);
            JSONArray currenciesData = jsonObject.getJSONArray("result");
            for (int i = 0; i < currenciesData.length(); i++) {
                JSONObject currencyData = currenciesData.getJSONObject(i);
                currencyValue.put(currencyData.getString("MarketName"), currencyData.getDouble("Last"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        totalBalance = 0.0;

        for (CoinData coinData : coinDataList) {
            String currencyKey = "BTC-" + coinData.getCurrency();
            //btc is always in dollars
            if (coinData.getCurrency().equals("BTC")) {
                currencyKey = "USDT-" + coinData.getCurrency();
                double coinValue = currencyValue.get(currencyKey);
                double holding = (coinData.getHolding());
                double balance = coinValue * holding;
                coinData.setBalance((balance));
                coinData.setPrice(coinValue);
                totalBalance += holding;
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
                continue;
            }
            if (currencyValue.containsKey(currencyKey)) {
                double coinValue = currencyValue.get(currencyKey);
                double holding = (coinData.getHolding());
                double balance = coinValue * holding;
                totalBalance += balance;
                coinData.setBalance((balance));
                coinData.setPrice(coinValue);
            }
        }

        if (isDollars) {
            String USDBTC = "USDT-BTC";
            double coinValue = currencyValue.get(USDBTC);
            for (CoinData coinData : coinDataList) {
                //btc is always in dollars
                if (coinData.getCurrency().equals("BTC")) {
                    continue;
                }
                if (coinData.getCurrency().equals("USDT")) {
                    continue;
                }
                coinData.setBalance(coinData.getBalance() * coinValue);
                coinData.setPrice(coinData.getPrice() * coinValue);
            }
            totalBalance *= coinValue;
        }

        //Update the list with the second to second update on balances
        refreshPortfolioData(totalBalance);

    }

    private void refreshPortfolioData(double totalBalance) {
        String currency = isDollars ? "$" : "₿";
        if (isDollars) {
            String val = currency + new DecimalFormat("#.##").format(totalBalance);
            mTotalBalance.setText(val);
        } else {
            String val = currency + new DecimalFormat("#.#######").format(totalBalance);
            mTotalBalance.setText(val);
        }
        recyclerViewAdapter.notifyDataSetChanged();
        if (prevBalance == 0.0) {
        }
        //Negative change in portfolio balance
        else if (totalBalance < prevBalance) {
            Picasso.with(getContext()).load(R.drawable.sad_face).fit().into(mHappinessIndicator);
        } else if (totalBalance >= prevBalance) { //Positive change in portfolio
            Picasso.with(getContext()).load(R.drawable.happy_face).fit().into(mHappinessIndicator);
        }
        //Keep track of the totalBalance in prevBalance to monitor change
        prevBalance = totalBalance;
    }

    public void changeUnits() {
        isDollars = !isDollars;
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.setDollars(isDollars);
        }
    }

    public void sortBy(String sortingMethod) {
        if (sortingMethod.equals(TICKER)) {
            Collections.sort(coinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return coinData.getCurrency().compareTo(t1.getCurrency());
                }
            });
        }

        if (sortingMethod.equals(HOLDING)) {
            Collections.sort(coinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return Double.compare(t1.getHolding(), coinData.getHolding());
                }
            });
        }

        if (sortingMethod.equals(BALANCE)) {
            Collections.sort(coinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return Double.compare(t1.getBalance(), coinData.getBalance());
                }
            });
        }

        if (sortingMethod.equals(PRICE)) {
            Collections.sort(coinDataList, new Comparator<CoinData>() {
                @Override
                public int compare(CoinData coinData, CoinData t1) {
                    return Double.compare(t1.getPrice(), coinData.getPrice());
                }
            });
        }

        recyclerViewAdapter.notifyDataSetChanged();
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
        void onPortfolioListFragmentInteraction(CoinData item);
    }

    class RetrieveIconImage extends AsyncTask<Void, Void, Boolean> {

        private List<CoinData> mCoinData;

        public RetrieveIconImage(List<CoinData> cd) {
            this.mCoinData = cd;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String nonce = Long.toString(new Date().getTime());
            URL url = null;
            HttpURLConnection connection = null;
            try {
                String urlString = "https://www.cryptocompare.com/api/data/coinlist/";
                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

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
                        success = coinBalancesJson.getString("Response");
                        JSONObject data = coinBalancesJson.getJSONObject("Data");

                        for (CoinData coinData : mCoinData) {
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
                        success = "false";
                    }
                    if (success.equals("false")) {
                        return false;
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
