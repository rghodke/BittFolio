package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.OrderBookRecyclerViewAdapter;
import crypto.manager.bittfolio.model.OrderBookEntry;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderBookFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderBookFragment extends Fragment {
    private static final String ARG_ORDER_BOOK_JSON_STRING = "ORDER_HISTORY_JSON";
    private OrderBookRecyclerViewAdapter mRecyclerViewBuyAdapter, mRecyclerViewSellAdapter;


    // TODO: Rename and change types of parameters
    private String mOrderBookJSON;
    private List<OrderBookEntry> mOrderBookBuyEntries;
    private List<OrderBookEntry> mOrderBookSellEntries;
    private RecyclerView mRecyclerViewBuyList, mRecyclerViewSellList;
    private OrderBookFragmentInteractionListener mListener;


    public OrderBookFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderBookFragment newInstance() {
        OrderBookFragment fragment = new OrderBookFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //No menu for this fragment
        setRetainInstance(true);
        if (getArguments() != null) {
        }
    }

    //No menu for this fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        mOrderBookBuyEntries = new ArrayList<>();
        mOrderBookSellEntries = new ArrayList<>();

        mRecyclerViewBuyList = (RecyclerView) view.findViewById(R.id.buy_list);

        mRecyclerViewSellList = (RecyclerView) view.findViewById(R.id.sell_list);

        // Set the adapter
        Context context = view.getContext();
        mRecyclerViewBuyList.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerViewBuyAdapter = new OrderBookRecyclerViewAdapter(mOrderBookBuyEntries);
        mRecyclerViewBuyList.setAdapter(mRecyclerViewBuyAdapter);

        mRecyclerViewSellList.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerViewSellAdapter = new OrderBookRecyclerViewAdapter(mOrderBookSellEntries);
        mRecyclerViewSellList.setAdapter(mRecyclerViewSellAdapter);


        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OrderBookFragmentInteractionListener) {
            mListener = (OrderBookFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPortfolioListFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void updateOrderBookHistory(String stringExtra) {
        mOrderBookBuyEntries = new ArrayList<>();
        mOrderBookSellEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONObject orderHistoryJSON = jsonObject.getJSONObject("result");
            JSONArray orderHistoryBuyJSON = orderHistoryJSON.getJSONArray("buy");
            JSONArray orderHistorySellJSON = orderHistoryJSON.getJSONArray("sell");
            for (int i = 0; i < orderHistoryBuyJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryBuyJSON.getJSONObject(i);
                mOrderBookBuyEntries.add(new OrderBookEntry(orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("Rate")));
            }
            for (int i = 0; i < orderHistorySellJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistorySellJSON.getJSONObject(i);
                mOrderBookSellEntries.add(new OrderBookEntry(orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("Rate")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderBookData();
    }


    private void refreshOrderBookData() {
        mRecyclerViewBuyAdapter.updateData(mOrderBookBuyEntries);
        mRecyclerViewSellAdapter.updateData(mOrderBookSellEntries);
        mListener.dismissProgressDialog();
    }

    public interface OrderBookFragmentInteractionListener {
        void dismissProgressDialog();

    }
}
