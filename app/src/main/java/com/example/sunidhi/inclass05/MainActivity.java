package com.example.sunidhi.inclass05;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    TextView keywordTextView;
    Button goButton;
    ImageButton imageButtonPrev, imageButtonNext;
    ProgressDialog progressDialog;
    String keyword = "";
    String tempUrl ="";
    String[] arr;
    String result;
    ImageView photoImageView;

    static int count = 0;
    static int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        keywordTextView = findViewById( R.id.keywordTextView );
        goButton = findViewById( R.id.goButton );
        imageButtonPrev = findViewById( R.id.imageButtonPrev );
        imageButtonNext = findViewById( R.id.imageButtonNext );

        photoImageView = findViewById( R.id.photoImageView );

        imageButtonNext.setEnabled( false );
        imageButtonPrev.setEnabled( false );

        goButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()){
                    arr = null;
                    new GetDataAsync().execute( "http://dev.theappsdr.com/apis/photos/keywords.php" );
                }
                else{
                    Toast.makeText( MainActivity.this, "No Internet Access", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

        imageButtonNext.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count < arr.length - 1){
                    count++;
                }
                else{
                    count = 0;
                }

                new GetImageAsync(photoImageView).execute(arr[count].toString());
            }
        } );

        imageButtonPrev.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count == 0){
                    count = arr.length -1;
                }
                else{
                    count --;
                }
                new GetImageAsync(photoImageView).execute(arr[count].toString());
            }
        } );
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    class GetDataAsync extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            String result = null;
            try {
                URL url = new URL( strings[0] );
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = IOUtils.toString( connection.getInputStream(), "UTF-8" );
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                final String[] arr = s.split( ";" );

                AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
                builder.setTitle( "Select Keywords" );
                builder.setItems( arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        keyword = arr[i];
                        keywordTextView.setText( arr[i] );
                        RequestParams params = new RequestParams();
                        params.addParameter( "keyword", keyword );
                        new GetPhotosAsync(params).execute( "http://dev.theappsdr.com/apis/photos/index.php" );
                    }
                } ).show();
            }
        }
    }

    class GetPhotosAsync extends AsyncTask<String, Void, String>{
        RequestParams mparams;
        public GetPhotosAsync(RequestParams params) {
            mparams = params;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            String result = null;
            try {
                String strUrl = "http://api.theappsdr.com/params.php" + "?" +
                        "keyword=" + URLEncoder.encode(keyword, "UTF-8");
                //Log.d( "demo", "URL = " +strUrl );
                URL url = new URL(mparams.getEncodedUrl( strUrl ));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = IOUtils.toString(connection.getInputStream(), "UTF8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String s) {
            new GetSimpleAsync().execute( "http://dev.theappsdr.com/apis/photos/index.php?keyword=" + keyword );



        }
    }

    private class GetSimpleAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
             result = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = IOUtils.toString(connection.getInputStream(), "UTF-8");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                arr = result.split( "\\s+" );
                new GetImageAsync(photoImageView).execute(arr[count].toString());
                if (result.length() != 0){
                    imageButtonPrev.setEnabled( true );
                    imageButtonNext.setEnabled( true );
                }
            } else {
                Log.d("demo", "null result");
            }
        }
    }

    class GetImageAsync extends AsyncTask<String, Void, Void>{

        ImageView imageView;
        Bitmap bitmap;

        public GetImageAsync(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading Image");
            progressDialog.setCancelable(false);
            progressDialog.setMax(10000);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                   bitmap = BitmapFactory.decodeStream( connection.getInputStream() );
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if ((bitmap != null) && (imageView != null)){
                imageView.setImageBitmap( bitmap );
            }
        }
    }

}
