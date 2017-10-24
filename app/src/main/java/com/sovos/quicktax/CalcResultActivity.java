package com.sovos.quicktax;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * Created by Bart JV on 12/29/2016.
 */
public class CalcResultActivity extends AppCompatActivity {

    public static void fillTaxTables(JSONArray trsResponse, LayoutInflater inflater, TableLayout tableLayout, TextView taxesAmountTextView, TextView totalAmountTextView, Double grossAmount, ProgressBar progressBar){

        View tableRow = null;
        TextView taxingAuthority = null;
        TextView taxAmount = null;
        String taxingAuthorityString = "";
        Double rate = 0.0;
        Double taxSum = 0.0;
        DecimalFormat doubleFormat = new DecimalFormat("#.##");
        String output = "";
        progressBar.setVisibility(View.GONE);

        JSONObject singleTRSValue = new JSONObject();
        JSONObject filteredTRSObject = new JSONObject();;
        for(int i = 0; i < trsResponse.length(); i++){
            try {
                singleTRSValue = trsResponse.getJSONObject(i);
                taxingAuthorityString = singleTRSValue.getString("taxingAuthority");
                rate = singleTRSValue.getDouble("rate")/100;
                if(filteredTRSObject.has(taxingAuthorityString)){
                    rate += filteredTRSObject.getDouble(taxingAuthorityString);
                    filteredTRSObject.remove(taxingAuthorityString);
                }
                taxSum+=rate;
                filteredTRSObject.put(taxingAuthorityString, rate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Iterator<String> keys = filteredTRSObject.keys();
        while(keys.hasNext()){
            tableRow = inflater.inflate(R.layout.row_result_tax, tableLayout, false);
            taxingAuthority = (TextView) tableRow.findViewById(R.id.text_taxing_authority);
            taxAmount = (TextView) tableRow.findViewById(R.id.text_tax_amount);
            taxingAuthorityString = (String)keys.next();
            try {
                rate = filteredTRSObject.getDouble(taxingAuthorityString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            taxingAuthority.setText(StringUtils.abbreviate(taxingAuthorityString,35));
            output = doubleFormat.format(rate * grossAmount);
            taxAmount.setText(output);
            tableLayout.addView(tableRow);
        }

        taxesAmountTextView.setText(doubleFormat.format(taxSum*grossAmount));
        totalAmountTextView.setText(doubleFormat.format(taxSum*grossAmount+grossAmount));
    }

    public static void fillTaxTables(JSONObject tweResponse, LayoutInflater inflater, TableLayout tableLayout, TextView taxesAmountTextView, TextView totalAmountTextView, Double grossAmount, ProgressBar progressBar){

        View tableRow = null;
        TextView taxingAuthority = null;
        TextView taxAmount = null;
        String taxingAuthorityString = "";
        Double txAmt = 0.0;
        Double taxTotal = 0.0;
        DecimalFormat doubleFormat = new DecimalFormat("#.##");
        String output = "";
        progressBar.setVisibility(View.GONE);

        JSONObject singleTRSValue = new JSONObject();
        JSONObject filteredTaxNameAmtObject = new JSONObject();;
        try {
            filteredTaxNameAmtObject = tweResponse.getJSONObject("txNameAmt");
            taxTotal = tweResponse.getDouble("totalTaxAmt");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Iterator<String> keys = filteredTaxNameAmtObject.keys();
        while(keys.hasNext()){
            tableRow = inflater.inflate(R.layout.row_result_tax, tableLayout, false);
            taxingAuthority = (TextView) tableRow.findViewById(R.id.text_taxing_authority);
            taxAmount = (TextView) tableRow.findViewById(R.id.text_tax_amount);
            taxingAuthorityString = (String)keys.next();
            try {
                txAmt = filteredTaxNameAmtObject.getDouble(taxingAuthorityString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            taxingAuthority.setText(StringUtils.abbreviate(taxingAuthorityString,35));
            taxAmount.setText(doubleFormat.format(txAmt));
            tableLayout.addView(tableRow);
        }

        taxesAmountTextView.setText(doubleFormat.format(taxTotal));
        totalAmountTextView.setText(doubleFormat.format(taxTotal+grossAmount));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DecimalFormat doubleFormat = new DecimalFormat("#.##");
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.GROSS_AMOUNT);
        String latlng = intent.getStringExtra(MainActivity.LAT_LNG);
        Double grossAmount = Double.parseDouble(message);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc_result);
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table_calc_result);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView taxesAmountTextView = (TextView) findViewById(R.id.text_taxes_amount);
        TextView totalAmountTextView = (TextView) findViewById(R.id.text_total_amount);
        TextView grossAmountTextView = (TextView) findViewById(R.id.text_gross_amount);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        totalAmountTextView.setText(doubleFormat.format(grossAmount));
        grossAmountTextView.setText(doubleFormat.format(grossAmount));

        GoogleRestClient.getAddress(this.getApplicationContext(), latlng, inflater, tableLayout, taxesAmountTextView, totalAmountTextView, grossAmount, progressBar);
    }

}
