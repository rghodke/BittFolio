package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crypto.manager.bittfolio.adapter.MyCoinRecyclerViewAdapter;
import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.model.CoinData;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PortfolioFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_BALANCES_JSON_STRING = "coin_balances";
    // TODO: Customize parameters
    private String mCoinBalanceString;
    private OnListFragmentInteractionListener mListener;
    private List<CoinData> coinDataList;
    private MyCoinRecyclerViewAdapter recyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PortfolioFragment() {
    }

    // TODO: Customize parameter initialization
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


        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerViewAdapter = new MyCoinRecyclerViewAdapter(coinDataList, mListener);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
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

        return coinDataList;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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


        for (CoinData coinData : coinDataList) {
            String currencyKey = "BTC-" + coinData.getCurrency();
            if (currencyValue.containsKey(currencyKey)) {
                double coinValue = currencyValue.get(currencyKey);
                double holding = (coinData.getHolding());
                double balance = coinValue * holding;
                coinData.setBalance((balance));
            }
        }

        //Update the list with the second to second update on balances
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(CoinData item);
    }
}