package crypto.manager.bittfolio.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.activity.CoinDataActivity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransferFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends Fragment {

    private TextView mDepositTextView;
    private ImageView mWalletQRCode;
    private EditText mWalletId;
    private Button mSendButton;
    private EditText mQuantityEditText;
    private TransferFragmentInteractionListener mListener;


    public TransferFragment() {
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
    public static TransferFragment newInstance() {
        TransferFragment fragment = new TransferFragment();
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
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        mDepositTextView = view.findViewById(R.id.text_view_wallet_id);
        mWalletQRCode = view.findViewById(R.id.qrcode_wallet);
        mWalletId = view.findViewById(R.id.withdraw_wallet_id);
        mSendButton = view.findViewById(R.id.button_send);
        mQuantityEditText = view.findViewById(R.id.edit_text_quantity);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantityEditText != null && mWalletId != null) {
                    String quantity = mQuantityEditText.getText().toString();
                    String walletId = mWalletId.getText().toString();
                    if (!quantity.isEmpty() && !walletId.isEmpty())
                        ((CoinDataActivity) getActivity()).startSendTransaction(quantity, walletId);
                }
            }
        });


        return view;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TransferFragmentInteractionListener) {
            mListener = (TransferFragmentInteractionListener) context;
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
        mListener = null;
        super.onDetach();
    }

    /**
     * Parses the json string and retrieves the deposit id. It then creates a qrcode with the id
     * as well.
     *
     * @param s
     */
    public void updateDepositAddress(String s) {
        try {
            JSONObject depositAddressJson = new JSONObject(s);
            JSONObject depositAddressResultJson = depositAddressJson.getJSONObject("result");
            String depositAddress = depositAddressResultJson.getString("Address");
            mDepositTextView.setText(depositAddress);

            Bitmap bm = encodeAsBitmap(depositAddress);
            if (bm != null && mWalletQRCode != null) {
                mWalletQRCode.setImageBitmap(bm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        mListener.dismissProgressDialog();
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 500, 500, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    public void updateWalletID(String contents) {
        mWalletId.setText(contents);
    }

    public interface TransferFragmentInteractionListener {
        void dismissProgressDialog();
    }
}
