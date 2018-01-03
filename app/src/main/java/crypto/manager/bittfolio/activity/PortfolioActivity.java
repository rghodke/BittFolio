package crypto.manager.bittfolio.activity;

import android.support.v4.app.FragmentActivity;

import android.os.Bundle;

import crypto.manager.bittfolio.R;
import crypto.manager.bittfolio.fragment.PortfolioFragment;
import crypto.manager.bittfolio.model.CoinData;

public class PortfolioActivity extends FragmentActivity implements PortfolioFragment.OnListFragmentInteractionListener{

    private PortfolioFragment mProfolioFragment;
    private static final String TAG_PORTFOLIO_FRAGMENT = "PORTFOLIO-FRAGMENT";
    private static final String EXTRA_COIN_BALANCE_STRING = "EXTRA_COIN_BALANCE_STRING";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        String coinBalanceString = "";
        if(getIntent().hasExtra(EXTRA_COIN_BALANCE_STRING)){
            coinBalanceString = getIntent().getStringExtra(EXTRA_COIN_BALANCE_STRING);
        }


        if(savedInstanceState != null){
            mProfolioFragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(TAG_PORTFOLIO_FRAGMENT);
        }else{
            mProfolioFragment = PortfolioFragment.newInstance(coinBalanceString);
        }

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mProfolioFragment, TAG_PORTFOLIO_FRAGMENT).commit();


    }

    @Override
    public void onListFragmentInteraction(CoinData item) {
        System.out.println(item.getCurrency());
    }
}
