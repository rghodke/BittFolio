package crypto.manager.bittfolio;

import android.app.Application;

/**
 * Created by ghodk on 1/7/2018.
 */

public class Globals extends Application {
    private String mApiKey;
    private String mApiSecret;

    public String getApiKey() {
        return mApiKey;
    }

    public void setApiKey(String mApiKey) {
        this.mApiKey = mApiKey;
    }

    public String getApiSecret() {
        return mApiSecret;
    }

    public void setApiSecret(String mApiSecret) {
        this.mApiSecret = mApiSecret;
    }
}
