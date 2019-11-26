package com.pritam.productflavors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String, Object>> aList = new ArrayList<>();
    View view;
    ListView listView;
    aListAdapter adapter;

    //ProgressDialog progress;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        view = getWindow().getDecorView().getRootView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchData();
            }
        });

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (isNetworkAvaiable()) {
            fetchData();
        } else {
            Snackbar.make(view, "No Internet Available", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchData() {
        try {
//            progress.show();
            String url = BuildConfig.SERVER_URL + "repos?sort=updated&per_page=25";

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            loggerPrint(request.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
//                    progress.dismiss();
                    Log.d("-->>", getStackTrace(e));
                    alertDialog("Request Failure", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, final Response response) {
//                    progress.dismiss();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (response.isSuccessful()) {
                                try {
                                    String s = response.body().string();
                                    aList = new ArrayList<>();
                                    JsonArray jsonArr = (JsonArray) (new JsonParser()).parse(s);
                                    for (int i = 0; i < jsonArr.size(); i++) {
                                        HashMap<String, Object> hm = new HashMap<>();
                                        JsonObject jobj = (JsonObject) jsonArr.get(i);

                                        JsonElement str = jobj.get("name");
                                        if (str != null && (!str.isJsonNull())) {
                                            hm.put("name", str.getAsString());
                                        } else {
                                            hm.put("name", "");
                                        }
                                        str = jobj.get("description");
                                        if (str != null && (!str.isJsonNull())) {
                                            hm.put("description", str.getAsString());
                                        } else {
                                            hm.put("description", "");
                                        }
                                        str = jobj.get("html_url");
                                        if (str != null && (!str.isJsonNull())) {
                                            hm.put("html_url", str.getAsString());
                                        } else {
                                            hm.put("html_url", "");
                                        }
                                        aList.add(hm);

                                        ListViewRefresh();
                                    }
                                    loggerPrint(aList.toString());
                                } catch (Exception e) {
                                    loggerPrint(getStackTrace(e));
                                }
                            } else {
                                loggerPrint(response.toString() + "\n" + response.body().toString());
                                Toast.makeText(mContext, "Something went Wrong", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            });
        } catch (Exception e) {
//            progress.dismiss();
            loggerPrint(getStackTrace(e));
            alertDialog("Exception", getStackTrace(e));
        }
    }

    private void loggerPrint(String str) {
        if(BuildConfig.LOGGER){
            Log.d("-->>", str);
        }
    }


    private void ListViewRefresh() {
        if (aList.size() == 0) {
            Toast.makeText(mContext, "No result found", Toast.LENGTH_LONG).show();
        }

        // Getting a reference to listview of main.xml layout file
        listView = (ListView) view.findViewById(R.id.listview);
        adapter = new aListAdapter(MainActivity.this, aList);
        // adapter.notifyDataSetChanged();
        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

    }

    private boolean isNetworkAvaiable() {
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    public String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public void alertDialog(String title, String message) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                // Setting Dialog Title
                alertDialog.setTitle(title);

                // Setting Dialog Message
                alertDialog.setMessage(message);

                // Setting Icon to Dialog
                //alertDialog.setIcon(R.drawable.ic_launcher_foreground);
                alertDialog.setCancelable(true);

                // Showing Alert Message
                alertDialog.show();
            }
        });

    }
}
