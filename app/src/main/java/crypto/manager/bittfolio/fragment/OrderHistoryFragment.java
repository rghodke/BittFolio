package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.adapter.CoinRecyclerViewAdapter;
import crypto.manager.bittfolio.adapter.OrderHistoryRecyclerViewAdapter;
import crypto.manager.bittfolio.model.OrderHistoryEntry;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderHistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderHistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_ORDER_HISTORY_JSON_STRING = "ORDER_HISTORY_JSON";
    private OrderHistoryRecyclerViewAdapter recyclerViewAdapter;


    // TODO: Rename and change types of parameters
    private String mOrderHistoryJSON;
    private String mParam2;
    private List<OrderHistoryEntry> orderHistoryEntries;
    private RecyclerView recyclerView;

    public OrderHistoryFragment() {
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
    public static OrderHistoryFragment newInstance() {
        OrderHistoryFragment fragment = new OrderHistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_order_history_list, container, false);

        orderHistoryEntries = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAdapter = new OrderHistoryRecyclerViewAdapter(orderHistoryEntries);
        recyclerView.setAdapter(recyclerViewAdapter);

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


    public void updateOrderHistory(String stringExtra) {
        orderHistoryEntries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(stringExtra);
            JSONArray orderHistoryJSON = jsonObject.getJSONArray("result");
            for (int i = 0; i < orderHistoryJSON.length(); i++) {
                JSONObject orderHistoryEntry = orderHistoryJSON.getJSONObject(i);
                orderHistoryEntries.add(new OrderHistoryEntry(orderHistoryEntry.getString("OrderType"), orderHistoryEntry.getString("Quantity"), orderHistoryEntry.getString("QuantityRemaining"), orderHistoryEntry.getString("Price")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOrderHistoryData();
    }


    private void refreshOrderHistoryData() {
        recyclerViewAdapter = new OrderHistoryRecyclerViewAdapter(orderHistoryEntries);
        recyclerView.setAdapter(recyclerViewAdapter);
        //Cannot use notify dataset change since we re-create the list every refresh
//        recyclerViewAdapter.notifyDataSetChanged();
    }
}
