package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.activity.CoinDataActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {

    private Button mBuyButton, mSellButton;
    private EditText mBuyQuantityEditText, mSellQuantityEditText, mBuyPriceEditText, mSellPriceEditText;

    private TextView mBidPrice, mAskPrice, mLastPrice;


    public OrderFragment() {
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
    public static OrderFragment newInstance() {
        OrderFragment fragment = new OrderFragment();
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
        View view = inflater.inflate(R.layout.fragment_order, container, false);


        //Buy views
        mBuyButton = view.findViewById(R.id.buy_button);
        mBuyQuantityEditText = view.findViewById(R.id.edit_text_buy_quantity);
        mBuyPriceEditText = view.findViewById(R.id.edit_text_buy_price);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBuyQuantityEditText != null && mBuyPriceEditText != null) {
                    String quantity = mBuyQuantityEditText.getText().toString();
                    String price = mBuyPriceEditText.getText().toString();
                    if (!quantity.isEmpty() && !price.isEmpty())
                        ((CoinDataActivity) getActivity()).startBuyTransaction(quantity, price);
                }
            }
        });

        //Sell Views
        mSellButton = view.findViewById(R.id.sell_button);
        mSellQuantityEditText = view.findViewById(R.id.edit_text_sell_quantity);
        mSellPriceEditText = view.findViewById(R.id.edit_text_sell_price);

        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSellQuantityEditText != null && mSellPriceEditText != null) {
                    String quantity = mSellQuantityEditText.getText().toString();
                    String price = mSellPriceEditText.getText().toString();
                    if (!quantity.isEmpty() && !price.isEmpty())
                        ((CoinDataActivity) getActivity()).startSellTransaction(quantity, price);
                }
            }
        });


        mBidPrice = view.findViewById(R.id.text_view_current_bid_price);
        mAskPrice = view.findViewById(R.id.text_view_current_ask_price);
        mLastPrice = view.findViewById(R.id.text_view_current_last_price);

        // Set the adapter
        Context context = view.getContext();

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

    public void updatePrice(String currencyDetails) {
        try {
            JSONObject jsonObject = new JSONObject(currencyDetails);
            JSONObject innerObj = jsonObject.getJSONObject("result");
            double bid = innerObj.getDouble("Bid");
            double ask = innerObj.getDouble("Ask");
            double last = innerObj.getDouble("Last");

            DecimalFormat df = new DecimalFormat("#.########");

            mBidPrice.setText(df.format(bid));
            mAskPrice.setText(df.format(ask));
            mLastPrice.setText(df.format(last));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
