package com.saitorhan.harmfulconnections;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.saitorhan.harmfulconnections.database.DbCRUD;
import com.saitorhan.harmfulconnections.database.DbProcessor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DownloadActivity extends AppCompatActivity {

    ProgressBar downProgressBar;
    ProgressBar insertProgressBar;
    TextView textViewStatu;
    ProgressBar progressBarDownloading;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        loadAd();

        downProgressBar = findViewById(R.id.progressBarDownload);
        insertProgressBar = findViewById(R.id.progressBarInsert);
        textViewStatu = findViewById(R.id.textViewStatu);
        progressBarDownloading = findViewById(R.id.progressBarDownloading);
        progressBarDownloading.setVisibility(View.GONE);
        imageButton = findViewById(R.id.imageButtonDownload);

        Intent intent = getIntent();
        boolean add = intent.getBooleanExtra("add", true);

        if (add) {
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            int downloadCount = sharedPreferences.getInt("downloadCount", 0);
            sharedPreferences.edit().putInt("downloadCount", downloadCount + 1).apply();
        }
    }

    private void loadAd() {
        AdView adView = findViewById(R.id.adViewDownloadBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    void downloadFile() {
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.execute(getString(R.string.source_link));
    }

    public void btnDownload(View view) {
        downloadFile();
    }

    class DownloadFile extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            publishProgress(-1);
            xmlParse xmlParse = new xmlParse();
            try {
                URL url = new URL(strings[0]);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(new InputSource(url.openStream()));
                document.getDocumentElement().normalize();

                Node rootNode = document.getElementsByTagName(getString(R.string.usom_data)).item(0);
                publishProgress(-2);
                //region xml info
                String update = "", author = "";
                Element childNodes = (Element) rootNode;
                NodeList xmlInfoElement = childNodes.getElementsByTagName(getString(R.string.xml_info)).item(0).getChildNodes();
                Node tempNode;
                for (int i = 0; i < xmlInfoElement.getLength(); i++) {
                    tempNode = xmlInfoElement.item(i);
                    if (tempNode == null || tempNode.getNodeName() == null) {
                        continue;
                    }
                    if (tempNode.getNodeName().equalsIgnoreCase("updated")) {

                        Node tempNode1 = tempNode.getChildNodes().item(0);
                        update = tempNode1.getNodeValue();
                        continue;
                    } else if (tempNode.getNodeName().equalsIgnoreCase("author")) {
                        Node tempNode1 = tempNode.getChildNodes().item(0);
                        author = tempNode1.getNodeValue();
                        continue;
                    }
                }
                Calendar calendar = Calendar.getInstance();
                String date = update.replace('-', '.') + " " + String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                xmlParse.setXmlInfo(new xmlInfo(date, author));
                //endregion xml info

                //region url infos

                NodeList urlList = ((Element) (((Element) rootNode).getElementsByTagName(getString(R.string.url_list)).item(0))).getElementsByTagName(getString(R.string.url_info));
                UrlInfo[] urlInfos = new UrlInfo[urlList.getLength()];
                downProgressBar.setMax(urlInfos.length);
                NodeList urlInfosNode;
                String dateString = "";
                for (int i = 0; i < urlInfos.length; i++) {
                    urlInfos[i] = new UrlInfo();
                    tempNode = urlList.item(i);
                    urlInfosNode = tempNode.getChildNodes();
                    for (int j = 0; j < urlInfosNode.getLength(); j++) {
                        tempNode = urlInfosNode.item(j);

                        if (tempNode == null || tempNode.getNodeName() == null) {
                            continue;
                        }
                        if (tempNode.getNodeName().equalsIgnoreCase("id")) {

                            Node tempNode1 = tempNode.getChildNodes().item(0);
                            urlInfos[i].set_id(tempNode1.getNodeValue());
                            continue;
                        } else if (tempNode.getNodeName().equalsIgnoreCase("url")) {
                            Node tempNode1 = tempNode.getChildNodes().item(0);
                            urlInfos[i].setUrl(tempNode1.getNodeValue());
                            continue;
                        } else if (tempNode.getNodeName().equalsIgnoreCase("desc")) {
                            Node tempNode1 = tempNode.getChildNodes().item(0);
                            urlInfos[i].setDesc(tempNode1.getNodeValue());
                            continue;
                        } else if (tempNode.getNodeName().equalsIgnoreCase("source")) {
                            Node tempNode1 = tempNode.getChildNodes().item(0);
                            urlInfos[i].setSource(tempNode1.getNodeValue());
                            continue;
                        } else if (tempNode.getNodeName().equalsIgnoreCase("date")) {
                            continue;
                        }

                    }

                    if (i % 100 == 0) {
                        publishProgress(new Integer[]{i + 1, 0});
                    }
                }
                xmlParse.setUrlInfos(urlInfos);
                //endregion url infos


            } catch (Exception e) {
                Exception exception = e;
                return null;
            }

            //return xmlParse;
            publishProgress(-3);
            DbCRUD dbCRUD = new DbCRUD(getApplicationContext(), true);
            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", "1");
            contentValues.put("updated", xmlParse.getXmlInfo().getUpdated());
            contentValues.put("author", xmlParse.getXmlInfo().getAuthor());
            dbCRUD.database.delete(getString(R.string.table_xml_info), null, null);
            dbCRUD.database.insert(getString(R.string.table_xml_info), null, contentValues);

            contentValues.clear();
            dbCRUD.database.delete(getString(R.string.table_url_list), null, null);

            int length = xmlParse.getUrlInfos().length;
            insertProgressBar.setMax(length);

            UrlInfo urlInfo;
            String sql = "INSERT INTO urls(_id, url," + getResources().getString(R.string.column_description) + ", source) VALUES(?, ?, ?, ?)";
            dbCRUD.database.beginTransaction();
            SQLiteStatement sqLiteStatement = dbCRUD.database.compileStatement(sql);
            for (int i = 0; i < length; i++) {
                urlInfo = xmlParse.getUrlInfos()[i];
                sqLiteStatement.bindString(1, urlInfo.get_id());
                sqLiteStatement.bindString(2, urlInfo.getUrl());
                sqLiteStatement.bindString(3, translateUrlDesc(urlInfo.getDesc()));
                sqLiteStatement.bindString(4, urlInfo.getSource());
                sqLiteStatement.execute();
                sqLiteStatement.clearBindings();

                /*contentValues.clear();
                contentValues.put("_id", urlInfo.get_id());
                contentValues.put("url", urlInfo.getUrl());
                contentValues.put("description", urlInfo.getDesc());
                contentValues.put("source", urlInfo.getSource());
                dbCRUD.database.insert("urls", null, contentValues);*/


                if (i % 100 == 0) {
                    publishProgress(new Integer[]{downProgressBar.getMax(), i + 1});
                }

            }
            dbCRUD.database.setTransactionSuccessful();
            dbCRUD.database.endTransaction();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(getApplicationContext(), result ? getString(R.string.download_success) : getString(R.string.download_error), Toast.LENGTH_LONG).show();

            super.onPostExecute(result);

            if (result) {
                DownloadActivity.this.finish();
            } else {
                imageButton.setEnabled(true);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == -1) {
                progressBarDownloading.setVisibility(View.VISIBLE);
                imageButton.setEnabled(false);
                textViewStatu.setText(R.string.downloading);
                return;
            } else if (values[0] == -2) {
                textViewStatu.setText(R.string.parsing);
                // progressBarDownloading.setVisibility(View.GONE);
                return;
            } else if (values[0] == -3) {
                textViewStatu.setText(R.string.inserting);
                //  progressBarDownloading.setVisibility(View.GONE);
                return;
            }


            if (downProgressBar.getMax() != downProgressBar.getProgress()) {
                downProgressBar.setProgress(downProgressBar.getMax() - values[0] < 0 ? downProgressBar.getMax() : values[0]);
            }

            insertProgressBar.setProgress(insertProgressBar.getMax() - values[1] < 0 ? insertProgressBar.getMax() : values[1]);
            super.onProgressUpdate(values);
        }
    }

    private String translateUrlDesc(String desc) {
        switch (desc) {
            case "Bankacılık - Oltalama":
                return getResources().getString(R.string.banking);
            case "Zararlı Yazılım Barındıran/Yayan URL":
                return getResources().getString(R.string.badurl);
            case "Zararlı Yazılım Barındıran/Yayan Alan Adı":
                return getResources().getString(R.string.baddomain);
            case "Oltalama":
                return getResources().getString(R.string.phishing);
            case "Zararlı Yazılım - Komuta Kontrol Merkezi":
                return getResources().getString(R.string.malsoftware);
            case "Zararlı Yazılım Barındıran/Yayan IP":
                return getResources().getString(R.string.badip);
            case "Siber Saldırı (Port Tarama, Kaba Kuvvet vb.)":
                return getResources().getString(R.string.portscan);
            case "Zararlı Yazılım Barındıran/Yayan IP ":
                return getResources().getString(R.string.badappip);
            case "Zararlı Yazılım Barındıran/Yayan Alan Adı ":
                return getResources().getString(R.string.baddomain);
        }
        // Log.e("desc", desc);
        return desc;
    }
}
