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
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.saitorhan.harmfulconnections.database.DbCRUD;

public class AboutActivity extends AppCompatActivity {

    RewardedVideoAd rewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AdView adView = findViewById(R.id.adViewAboutBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView = findViewById(R.id.adViewAboutBannerUp);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(rewardedVideoAdListener);
        rewardedVideoAd.loadAd(getString(R.string.odul), new AdRequest.Builder().build());

        getDatas();
    }

    @Override
    protected void onResume() {
        getDatas();
        super.onResume();
    }

    private void getDatas() {
        TextView textViewUpdate = findViewById(R.id.textViewUpdateDate);
        TextView textViewRecordCount = findViewById(R.id.textViewRecordCount);

        DbCRUD dbCRUD = new DbCRUD(this, false);

        Cursor query = dbCRUD.database.query(getString(R.string.table_xml_info), new String[]{"updated"}, null, null, null, null, null);
        if (query.getCount() > 0) {
            query.moveToFirst();
            String update = query.getString(0);
            textViewUpdate.setText(update);
        } else {
            textViewUpdate.setText(getString(R.string.not_found));
        }

        query = dbCRUD.database.query(getString(R.string.table_url_list), new String[]{"_id"}, null, null, null, null, null);
        textViewRecordCount.setText(String.valueOf(query.getCount()));
    }

    public void updateDatabase(View view) {
        openDownloadActivity(true);
    }

    public void rateApplication(View view) {
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
        finish();
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
}
