package crypto.manager.bittfolio;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

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

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

}
