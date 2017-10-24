package com.sovos.quicktax;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by Bart JV on 12/29/2016.
 */

public class TWERestClient {
    private static final String BASE_URL_GEOCODE = "http://10.1.101.225:8380/Twe/api/rest/geoCode/single";
    private static final String BASE_URL_TAX = "http://10.1.101.225:8380/Twe/api/rest/calcTax/doc";
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static void post(Context context,String url, StringEntity entity, AsyncHttpResponseHandler responseHandler){
        client.post(context, url, entity,"application/json", responseHandler);
    }

    public static void getGeoCode(Context context, String city, String stateProv, String postalCode, String country, String stNameNum, LayoutInflater inflater, TableLayout tableLayout, TextView taxesAmountTextView, TextView totalAmountTextView, Double grossAmount, ProgressBar progressBar){
        final Context finalContext = context.getApplicationContext();
        final LayoutInflater finalInflater = inflater;
        final TableLayout finalTableLayout = tableLayout;
        final TextView finalTaxesAmountTextView = taxesAmountTextView;
        final TextView finalTotalAmountTextView = totalAmountTextView;
        final Double finalGrossAmount = grossAmount;
        final ProgressBar finalProgressBar = progressBar;
        JSONObject params = new JSONObject();
        StringEntity entity = null;
        try {
            params.put("usrname", "Admin");
            params.put("pswrd", "Admin123");
            params.put("city", city);
            params.put("stateProv", stateProv);
            params.put("pstlCd", postalCode);
            params.put("country", country);
            params.put("stNameNum", stNameNum);

            entity = new StringEntity(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        post(finalContext,BASE_URL_GEOCODE, entity, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject tweResponse){
                String responseGeoCode = "";
                try {
                    responseGeoCode = tweResponse.getString("geoCd");
                } catch (JSONException e) {
                    responseGeoCode = "";
                }
                getTaxRates(finalContext, responseGeoCode, finalInflater, finalTableLayout, finalTaxesAmountTextView, finalTotalAmountTextView, finalGrossAmount, finalProgressBar);
            }
        });

    }

    private static void getTaxRates(Context context, String geocode, LayoutInflater inflater, TableLayout tableLayout, TextView taxesAmountTextView, TextView totalAmountTextView, Double grossAmount, ProgressBar progressBar){
        final Context finalContext = context.getApplicationContext();
        final LayoutInflater finalInflater = inflater;
        final TableLayout finalTableLayout = tableLayout;
        final TextView finalTaxesAmountTextView = taxesAmountTextView;
        final TextView finalTotalAmountTextView = totalAmountTextView;
        final Double finalGrossAmount = grossAmount;
        final ProgressBar finalProgressBar = progressBar;
        JSONObject params = new JSONObject();
        JSONArray lines = new JSONArray();
        JSONObject lineParams = new JSONObject();
        StringEntity entity = null;
        try {
            params.put("usrname", "Admin");
            params.put("pswrd", "Admin123");
            params.put("isAudit", "false");
            params.put("rsltLvl", "5");
            params.put("trnSrc", "ERP");
            params.put("currn", "USD");
            params.put("docDt", "2016-11-09");
            params.put("txCalcTp", "1");
            params.put("tdcReqrd", "true");
            params.put("trnDocNum", "SalesTest1");

            lineParams.put("debCredIndr","1");
            lineParams.put("grossAmt",String.valueOf(grossAmount));
            lineParams.put("lnItmId","001");
            lineParams.put("qnty","1.0");
            lineParams.put("trnTp","1");
            lineParams.put("orgCd","LGSTEST");
            lineParams.put("sFGeoCd",geocode);
            lineParams.put("sTGeoCd",geocode);
            lines.put(lineParams);
            params.put("lines", lines);

            entity = new StringEntity(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        post(finalContext,BASE_URL_TAX, entity, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject tweResponse){
                JSONObject filteredTweResponse = new JSONObject();
                JSONObject txNameAmt = new JSONObject();
                JSONArray lineResults = new JSONArray();
                String txName = "";
                String txAmt = "";
                try {
                    lineResults = tweResponse.getJSONArray("lnRslts").getJSONObject(0).getJSONArray("jurRslts");

                    for(int i = 0; i<lineResults.length();i++){
                        txName = lineResults.getJSONObject(i).getString("txName");
                        txAmt = lineResults.getJSONObject(i).getString("txAmt");
                        txNameAmt.put(txName, txAmt);
                    }
                    filteredTweResponse.put("txNameAmt", txNameAmt);
                    txAmt = tweResponse.getString("txAmt");
                    filteredTweResponse.put("totalTaxAmt", txAmt);

                } catch (JSONException e) {
                }
                CalcResultActivity.fillTaxTables(filteredTweResponse,finalInflater, finalTableLayout, finalTaxesAmountTextView, finalTotalAmountTextView, finalGrossAmount, finalProgressBar);
            }
        });

    }
}
