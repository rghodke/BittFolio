package crypto.manager.bittfolio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LiveCoinValueService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();
    private final Context context = this;
    private final String LIVE_COIN_INTENT_EXTRA = "COIN_LAST_VALUES";
    private final String LIVE_COIN_INTENT_ACTION = "COIN_RETRIEVE_ACTION";

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LiveCoinValueService getService() {
            // Return this instance of LiveCoinValueService so clients can call public methods
            return LiveCoinValueService.this;
        }
    }

    private OkHttpClient client;

    @Override
    public IBinder onBind(Intent intent) {
        client = new OkHttpClient();
        return mBinder;
    }

    /**
     * method for clients
     */
    public void getLiveCoinValues() {

        Request request = new Request.Builder()
                .url("https://bittrex.com/api/v1.1/public/getmarketsummaries")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String currenciesJSONString = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(currenciesJSONString);
                        if (jsonObject.getString("success").equals("true")) {
                            Intent intent = new Intent();
                            intent.putExtra(LIVE_COIN_INTENT_EXTRA, currenciesJSONString);
                            intent.setAction(LIVE_COIN_INTENT_ACTION);
                            context.sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}