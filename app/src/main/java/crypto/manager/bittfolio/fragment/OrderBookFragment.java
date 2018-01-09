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
    private OrderBookRecyclerViewAdapter recyclerViewBuyAdapter, recyclerViewSellAdapter;


    // TODO: Rename and change types of parameters
    private String mOrderBookJSON;
    private List<OrderBookEntry> orderBookBuyEntries;
    private List<OrderBookEntry> orderBookSellEntries;
    private RecyclerView recyclerViewBuyList, recyclerViewSellList;

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
        setRetainInstance(true);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        orderBookBuyEntries = new ArrayList<>();
        orderBookSellEntries = new ArrayList<>();

        recyclerViewBuyList = (RecyclerView) view.findViewById(R.id.buy_list);

        recyclerViewSellList = (RecyclerView) view.findViewById(R.id.sell_list);

        // Set the adapter
        Context context = view.getContext();
        recyclerViewBuyList.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewBuyAdapter = new OrderBookRecyclerViewAdapter(orderBookBuyEntries);
        recyclerViewBuyList.setAdapter(recyclerViewBuyAdapter);

        recyclerViewSellList.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewSellAdapter = new OrderBookRecyclerViewAdapter(orderBookSellEntries);
        recyclerViewSellList.setAdapter(recyclerViewSellAdapter);

        return view;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void updateOrderBookHistory(String stringExtra) {
        orderBookBuyEntries = new ArrayList<>();
        orderBookSellEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONObject orderHistoryJSON = jsonObject.getJSONObject("result");
            JSONArray orderHistoryBuyJSON = orderHistoryJSON.getJSONArray("buy");
            JSONArray orderHistorySellJSON = orderHistoryJSON.getJSONArray("sell");
            for (int i = 0; i < orderHistoryBuyJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryBuyJSON.getJSONObject(i);
                orderBookBuyEntries.add(new OrderBookEntry(orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("Rate")));
            }
            for (int i = 0; i < orderHistorySellJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistorySellJSON.getJSONObject(i);
                orderBookSellEntries.add(new OrderBookEntry(orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("Rate")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderBookData();
    }


    private void refreshOrderBookData() {
        recyclerViewBuyAdapter.updateData(orderBookBuyEntries);
        recyclerViewSellAdapter.updateData(orderBookSellEntries);
    }
}
