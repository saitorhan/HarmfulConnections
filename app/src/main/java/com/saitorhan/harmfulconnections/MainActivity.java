package com.saitorhan.harmfulconnections;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.saitorhan.harmfulconnections.database.DbCRUD;

public class MainActivity extends AppCompatActivity {

    EditText editTextSearch;
    ListView listView;
    InterstitialAd interstitialAd;
    RewardedVideoAd rewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        loadAd();
        checkData();
        bindControls();
    }

    @Override
    protected void onResume() {
        getRandom38();
        super.onResume();
    }

    private void loadAd() {
        AdView adView = findViewById(R.id.adViewMainBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.gecis));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(interstitialAdListener);

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(rewardedVideoAdListener);
        rewardedVideoAd.loadAd(getString(R.string.odul), new AdRequest.Builder().build());
    }

    AdListener interstitialAdListener = new AdListener() {
        @Override
        public void onAdClosed() {
            interstitialAd.loadAd(new AdRequest.Builder().build());
            super.onAdClosed();
        }
    };

    RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {


        @Override
        public void onRewardedVideoAdLoaded() {
        }

        @Override
        public void onRewardedVideoAdOpened() {

        }

        @Override
        public void onRewardedVideoStarted() {

        }

        @Override
        public void onRewardedVideoAdClosed() {
            rewardedVideoAd.loadAd(getString(R.string.odul), new AdRequest.Builder().build());
        }

        @Override
        public void onRewarded(RewardItem rewardItem) {

            openDownloadActivity(false);
        }

        @Override
        public void onRewardedVideoAdLeftApplication() {

        }

        @Override
        public void onRewardedVideoAdFailedToLoad(int i) {
            rewardedVideoAd.loadAd(getString(R.string.odul), new AdRequest.Builder().build());
        }

        @Override
        public void onRewardedVideoCompleted() {

        }
    };

    private void checkData() {
        DbCRUD dbCRUD = new DbCRUD(this, false);
        Cursor query = dbCRUD.database.query(getString(R.string.table_url_list), new String[]{"_id"}, null, null, null, null, null);
        if (query.getCount() > 0) {
            return;
        }

        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setTitle(getString(R.string.app_name));
        alBuilder.setMessage(getString(R.string.no_data));
        alBuilder.setIcon(getResources().getDrawable(R.drawable.icon));
        alBuilder.setPositiveButton(getString(R.string.yes), yesClick);
        alBuilder.setPositiveButtonIcon(getResources().getDrawable(R.drawable.save));
        alBuilder.setNegativeButton(getString(R.string.no), noClick);
        alBuilder.setNegativeButtonIcon(getResources().getDrawable(R.drawable.cancel));
        alBuilder.show();
    }

    DialogInterface.OnClickListener yesClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            openDownloadActivity(true);
        }
    };
    DialogInterface.OnClickListener noClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

        }
    };

    private void bindControls() {
        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    searchUrl(null);
                    return true;
                }
                return false;
            }
        });
        listView = findViewById(R.id.list);

        getRandom38();
    }

    private void getRandom38() {
        DbCRUD dbCRUD = new DbCRUD(this, false);
        Cursor urls = dbCRUD.database.query(getString(R.string.table_url_list), new String[]{"_id", "url", getResources().getString(R.string.column_description)}, null, null, null, null, "RANDOM()", "20");

        if (urls.getCount() == 0) {
            Toast.makeText(this, getString(R.string.not_found), Toast.LENGTH_LONG).show();
            return;
        }

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.url_layout, urls, new String[]{"_id", "url", getResources().getString(R.string.column_description)}, new int[]{R.id.textViewid, R.id.textViewUrl, R.id.textViewDesc});
        listView.setAdapter(simpleCursorAdapter);
    }

    public void searchUrl(View view) {

        showInterstitialAd();

        DbCRUD dbCRUD = new DbCRUD(this, false);
        Cursor urls = dbCRUD.database.query("urls", new String[]{"_id", "url", getResources().getString(R.string.column_description)}, "url LIKE ?", new String[]{"%" + editTextSearch.getText().toString() + "%"}, null, null, "url");

        if (urls.getCount() == 0) {
            Toast.makeText(this, getString(R.string.not_found), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, urls.getCount() + " " + getString(R.string.link_count), Toast.LENGTH_LONG).show();
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.url_layout, urls, new String[]{"_id", "url", getResources().getString(R.string.column_description)}, new int[]{R.id.textViewid, R.id.textViewUrl, R.id.textViewDesc});
        listView.setAdapter(simpleCursorAdapter);
    }

    private void showInterstitialAd() {

        int searchCount = 0;
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        searchCount = sharedPreferences.getInt("searchCount", 1);

        if (searchCount++ % 5 == 0) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }

        sharedPreferences.edit().putInt("searchCount", searchCount).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download) {
            openDownloadActivity(true);
        } else if (item.getItemId() == R.id.about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.rateapp) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        } else if (item.getItemId() == R.id.shareapp) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    void openDownloadActivity(boolean addCount) {

        SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int downloadCount = sharedPreferences.getInt("downloadCount", 1);
        if (addCount && downloadCount % 5 == 0) {
            if (rewardedVideoAd.isLoaded()) {

                AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
                alBuilder.setTitle(getString(R.string.app_name));
                alBuilder.setMessage(getString(R.string.show_reward_video));
                alBuilder.setIcon(getResources().getDrawable(R.drawable.icon));
                alBuilder.setNegativeButton(getString(R.string.cancel), null);
                alBuilder.setNegativeButtonIcon(getResources().getDrawable(R.drawable.cancel));
                alBuilder.setPositiveButton(getString(R.string.watch), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rewardedVideoAd.show();
                    }
                });
                alBuilder.setPositiveButtonIcon(getResources().getDrawable(R.drawable.play));
                alBuilder.show();
            } else {
                Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
                intent.putExtra("add", false);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
            startActivity(intent);
        }
    }
}
