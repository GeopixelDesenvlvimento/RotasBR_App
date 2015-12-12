package com.petro.navigator.misc;

import android.content.Entity;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Clayton on 22/11/2015.
 */
public class HttpConnection {
    public static String getSetDataWeb(String url, String gID, String stateValue, String typeValue, String contentValue) throws JSONException {

        String answer = "";
        InputStream is = null;
        JSONObject jObj = null;
        String Val = "";
        String whereClause = "";

        try
        {
            if(stateValue != "" && !stateValue.equals("Selecione Estado"))
                whereClause += " uf='" + stateValue + "' and";
            if(typeValue != "" && !typeValue.equals("Selecione Classe"))
                whereClause += " classe='" + typeValue + "' and";
            if(contentValue != "" && !contentValue.equals("Selecione Contexto"))
                whereClause += " contexto='" + contentValue + "' and";

            whereClause += " gid > " + gID + " and";
            //if(Validade > 0) {
            //Val = String.valueOf(Validade);
            //whereClause += " val > '" + Val.substring(0, 4) + "-" + Val.substring(4, 6) + "-" + Val.substring(6, 8) + "' and";
            //}

            if(whereClause != "") {
                whereClause = whereClause.substring(0, whereClause.length() - 3);

                whereClause += " order by gid limit 1000";
            }

            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
            values.add(new BasicNameValuePair("tablename", "\"POI\""));
            values.add(new BasicNameValuePair("geocolumn", "geom"));
            values.add(new BasicNameValuePair("whereclause", whereClause));
            //if(Validade > 0) {
            //    values.add(new BasicNameValuePair("whereclause", "val>'" + Val + "'"));
            //}   else{
            //    values.add(new BasicNameValuePair("whereclause", "val>'2015-10-01' and gid>32400"));
            //}


            DefaultHttpClient httpClient = new DefaultHttpClient();
            String paramString = URLEncodedUtils.format(values, "utf-8");
            url += "?" + paramString;
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            answer = Utils.convertinputStreamToString(is);
            answer = answer.replaceAll("\"POI\"", "POI");

        }
        catch (NullPointerException e){ e.printStackTrace(); }
        catch (ClientProtocolException e){ e.printStackTrace(); }
        catch (IOException e){ e.printStackTrace(); }

        return answer;
    }

    public static String getDataSpinners(String url, String column) throws JSONException {

        String answer = "";
        InputStream is = null;
        JSONObject jObj = null;
        String Val = "";

        try
        {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
            values.add(new BasicNameValuePair("tablename", "\"POI\""));
            values.add(new BasicNameValuePair("column", column));


            DefaultHttpClient httpClient = new DefaultHttpClient();
            String paramString = URLEncodedUtils.format(values, "utf-8");
            url += "?" + paramString;
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            answer = Utils.convertinputStreamToString(is);
            answer = answer.replaceAll("\"POI\"", "POI");

        }
        catch (NullPointerException e){ e.printStackTrace(); }
        catch (ClientProtocolException e){ e.printStackTrace(); }
        catch (IOException e){ e.printStackTrace(); }

        return answer;
    }

    public static String getDataSpinnersWhereClause(String url, String column, String whereClause) throws JSONException {

        String answer = "";
        InputStream is = null;
        JSONObject jObj = null;
        String Val = "";

        try
        {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
            values.add(new BasicNameValuePair("tablename", "\"POI\""));
            values.add(new BasicNameValuePair("column", column));
            values.add(new BasicNameValuePair("whereclause", whereClause));


            DefaultHttpClient httpClient = new DefaultHttpClient();
            String paramString = URLEncodedUtils.format(values, "utf-8");
            url += "?" + paramString;
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            answer = Utils.convertinputStreamToString(is);
            answer = answer.replaceAll("\"POI\"", "POI");

        }
        catch (NullPointerException e){ e.printStackTrace(); }
        catch (ClientProtocolException e){ e.printStackTrace(); }
        catch (IOException e){ e.printStackTrace(); }

        return answer;
    }
}
