package com.sovos.quicktax;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Bart JV on 12/30/2016.
 */

public class GoogleRestClient {

    private static final String BASE_URL_GOOGLE = "https://maps.googleapis.com/maps/api/geocode/json";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static void get(Context context, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.get(context, BASE_URL_GOOGLE, params, responseHandler);
    }

    public static void getAddress(Context context, String latlng, LayoutInflater inflater, TableLayout tableLayout, TextView taxesAmountTextView, TextView totalAmountTextView, Double grossAmount, ProgressBar progressBar){
        final LayoutInflater finalInflater = inflater;
        final TableLayout finalTableLayout = tableLayout;
        final TextView finalTaxesAmountTextView = taxesAmountTextView;
        final TextView finalTotalAmountTextView = totalAmountTextView;
        final Double finalGrossAmount = grossAmount;
        final ProgressBar finalProgressBar = progressBar;
        final Context finalContext = context.getApplicationContext();
        RequestParams params = new RequestParams();
        params.put("latlng", latlng);


        get(finalContext, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject googleResponse){

                try {
                    String address = googleResponse.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                    String[] splitAddress = address.split(",");
                    String[] splitStateProv = splitAddress[2].split("\\s+");
                    String city = splitAddress[1];
                    String stateProv = splitStateProv[1];
                    String postalCode = splitStateProv[2];
                    String stNameNum = splitAddress[0];
                    TWERestClient.getGeoCode(finalContext,city,stateProv,postalCode,"UNITED STATES", stNameNum, finalInflater, finalTableLayout, finalTaxesAmountTextView, finalTotalAmountTextView, finalGrossAmount, finalProgressBar);
                } catch (JSONException e) {
                }
            }
        });

    }

}
