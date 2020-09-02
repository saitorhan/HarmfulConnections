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

import com.saitorhan.harmfulconnections.database.DbCRUD;

public class AboutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
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
        openDownloadActivity();
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

    void openDownloadActivity() {
        Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
        startActivity(intent);
    }

}
