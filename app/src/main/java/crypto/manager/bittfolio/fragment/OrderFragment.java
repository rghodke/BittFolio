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

    double btcUSDTValue = 1.0;
    private Button mBuyButton, mSellButton;
    private EditText mBuyQuantityEditText, mSellQuantityEditText, mBuyPriceEditText, mSellPriceEditText;
    private TextView mBidPrice, mAskPrice, mLastPrice;
    private boolean isDollars = false;

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
                    price = price.replaceAll("[^\\d.]", "");
                    double priceDouble = Double.parseDouble(price);
                    if (isDollars) {
                        priceDouble /= btcUSDTValue;
                    }
                    if (!quantity.isEmpty() && !price.isEmpty())
                        ((CoinDataActivity) getActivity()).startBuyTransaction(quantity, String.valueOf(priceDouble));
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
                    price = price.replaceAll("[^\\d.]", "");
                    double priceDouble = Double.parseDouble(price);
                    if (isDollars) {
                        priceDouble /= btcUSDTValue;
                    }
                    if (!quantity.isEmpty() && !price.isEmpty())
                        ((CoinDataActivity) getActivity()).startSellTransaction(quantity, String.valueOf(priceDouble));
                }
            }
        });

        View.OnClickListener updatePriceOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) view;
                String price = textView.getText().toString();
                mBuyPriceEditText.setText(price);
                mSellPriceEditText.setText(price);
            }
        };

        mBidPrice = view.findViewById(R.id.text_view_current_bid_price);
        mBidPrice.setOnClickListener(updatePriceOnClick);
        mAskPrice = view.findViewById(R.id.text_view_current_ask_price);
        mAskPrice.setOnClickListener(updatePriceOnClick);
        mLastPrice = view.findViewById(R.id.text_view_current_last_price);
        mLastPrice.setOnClickListener(updatePriceOnClick);

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

            String priceFormat = "#.########";

            if (isDollars) {
                bid *= btcUSDTValue;
                ask *= btcUSDTValue;
                last *= btcUSDTValue;
                priceFormat = "#.00";
            }

            DecimalFormat df = new DecimalFormat(priceFormat);

            String currency = isDollars ? "$" : "₿";

            mBidPrice.setText(currency + df.format(bid));
            mAskPrice.setText(currency + df.format(ask));
            mLastPrice.setText(currency + df.format(last));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void changeUnits() {
        isDollars = !isDollars;
    }

    public void updateBTCUSDTPrice(String btcUSDT) {
        try {
            JSONObject btcJson = new JSONObject(btcUSDT);
            JSONObject innerObj = btcJson.getJSONObject("result");
            btcUSDTValue = innerObj.getDouble("Last");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
