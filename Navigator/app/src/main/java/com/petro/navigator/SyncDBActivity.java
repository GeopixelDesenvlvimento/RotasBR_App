package com.petro.navigator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.BoringLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.petro.navigator.controller.Location;
import com.petro.navigator.misc.HttpConnection;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyncDBActivity extends AppCompatActivity {

    private Activity self;
    private Boolean insertBD = false;
    private Boolean erroJSON = false;
    private String ufSelected = null;
    private String classSelected = null;

    Spinner state;
    Spinner type;
    Spinner content;
    String[] namesSpinners;
    ArrayAdapter<String> dataAdapterUf;
    ArrayAdapter<String> dataAdapterType;
    ArrayAdapter<String> dataAdapterContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_db);

        //Seta Titulo da Activity
        setTitle("Atualizar Banco de Dados");
        self = this;
        AppManager.app = this;
        AppManager.setState(AppManager.STATE.Waiting);

        //Carregando os Spinners da Activity
        try {
            loadSipnnerUF();
        }catch (Exception error){

        }

        //loadSipnner();
    }

    /**
     * Evento de clique do botão Buscar Pois
     */
    public void clickUpdate(View v) throws IOException {

        try {
            AppManager.showView(self, R.id.sync_loading_db);
            AppManager.hideView(self, R.id.spnStateDB);
            AppManager.hideView(self, R.id.spnClassDB);
            AppManager.hideView(self, R.id.spnContexDBt);
            AppManager.hideView(self, R.id.btnUpdate);
            String stateValue = "Selecione Estado";
            String typeValue = "Selecione Classe";
            String contentValue = "Selecione Contexto";
            String whereClause = "";
            Location location = new Location(SyncDBActivity.this, false);

            Spinner stateS = (Spinner) findViewById(R.id.spnStateDB);
            if(stateS.getSelectedItem() != null) {
                stateValue = stateS.getSelectedItem().toString();
            }

            Spinner stateT = (Spinner) findViewById(R.id.spnClassDB);
            if(stateT.getSelectedItem() != null) {
                typeValue = stateT.getSelectedItem().toString();
            }

            Spinner stateC = (Spinner) findViewById(R.id.spnContexDBt);
            if(stateC.getSelectedItem() != null) {
                contentValue = stateC.getSelectedItem().toString();
            }

            //Pega o último resgistro do banco de dados TODO:Precisa melhorar o código, está aqui só pra apresentação no momento, mudar na versão final
            //List<LocationModel> listLocationModels = location.lastValue();
            //LocationModel locteste;
            int Validade = 0;

            if (stateValue != "" && !stateValue.equals("Selecione Estado"))
                whereClause += " uf='" + stateValue + "' and";
            if (typeValue != "" && !typeValue.equals("Selecione Classe"))
                whereClause += " type='" + typeValue + "' and";
            if (contentValue != "" && !contentValue.equals("Selecione Contexto"))
                whereClause += " context='" + contentValue + "' and";
            if (whereClause != "")
                whereClause = whereClause.substring(0, whereClause.length() - 3);

            location.deleteAll(whereClause);

            //if( listLocationModels.size() > 0 ) {
            //locteste = listLocationModels.get(0);
            //Validade = locteste.getDate();
            //}

            //Verifica se a Atualização pra fazer9
            verifyJSON(Validade, stateValue, typeValue, contentValue);
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }


    public String degenerateJSON(String data){

        String gid = "";
        String uf = "";
        String classe = "";
        String contexto = "";
        String nome = "";
        String lat = "";
        String lon = "";
        String val = "";
        // Insere os pontos em um vetor
        List<LocationModel> points = new ArrayList<LocationModel>();

        try
        {
            JSONObject jo = new JSONObject(data);
            JSONObject joFeatures;
            JSONArray ja;
            JSONObject joProperties;

            ja = jo.getJSONArray("features");

            //Se a lista retronada do Json for maior que 0 então é pra atualizar o banco de dados
            if(ja.length() > 0) {
                for(int tam = 0; tam < ja.length(); tam++){
                    joFeatures = ja.getJSONObject(tam);

                    joProperties = joFeatures.getJSONObject("properties");
                    gid = joProperties.getString("gid");
                    uf = joProperties.getString("uf");
                    classe = joProperties.getString("classe");
                    contexto = joProperties.getString("contexto");
                    nome = joProperties.getString("nome");
                    lat = joProperties.getString("lon");
                    lon = joProperties.getString("lat");
                    val = joProperties.getString("val");
                    val = val.replaceAll("-", "");
                    points.add(new LocationModel(uf, classe, contexto, nome, nome, Integer.parseInt(val), Float.parseFloat(lat), Float.parseFloat(lon)));
                }

                savePoints(points);
            }

        }
        catch (JSONException e)
        {
            //e.printStackTrace();
            return "erro";
        }

        return gid;
    }

    private void verifyJSON(final int Validade, final String stateValue, final String typeValue, final String contentValue){
        new Thread(){
            public void run(){
                String answer = null;
                try {
                    answer = HttpConnection.getSetDataWeb("http://74.208.229.211:8080/RotasBR/rest/json/getData", "0", stateValue, typeValue, contentValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(answer != "") {
                    while (answer != ""){
                        if(!answer.equals("erro")) {
                            String gid = degenerateJSON(answer);

                            try {
                                answer = HttpConnection.getSetDataWeb("http://74.208.229.211:8080/RotasBR/rest/json/getData", gid, stateValue, typeValue, contentValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            erroJSON = true;
                            insertBD = false;
                            mHandler.post(mtResults);
                            answer = "";
                        }
                    }
                    insertBD = true;
                    mHandler.post(mtResults);
                }
                else {
                    insertBD = false;
                    mHandler.post(mtResults);
                }
            }
        }.start();
    }

    private void savePoints(List<LocationModel> points){
        try {
            AppManager.location = new Location(SyncDBActivity.this, false);
            AppManager.location.InserDB(points);
        }catch (Exception error) {

        }
    }

    final Handler mHandlerSpinnersSelected = new Handler();
    final Handler mHandlerSpinnersUF = new Handler();
    final Handler mHandlerSpinners = new Handler();
    final Handler mHandler = new Handler();
    final Runnable mtResults = new Runnable() {
        public void run() {
            AppManager.hideView(self, R.id.sync_loading_db);
            if(!erroJSON){
                if(insertBD)
                    Utils.showSucces(SyncDBActivity.this, "Banco Atualizado com Sucesso!");
                else
                    Utils.showAlert(SyncDBActivity.this, "Não há informações novas para cadastro!");
            }
            else
                Utils.showError(SyncDBActivity.this, "Não foi possível buscar os dados no servidor, por favor entre em contato com a empresa!");

            AppManager.showView(self, R.id.btnUpdate);
            AppManager.showView(self, R.id.spnStateDB);
            AppManager.showView(self, R.id.spnClassDB);
            AppManager.showView(self, R.id.spnContexDBt);
        }
    };
    final Runnable mtResultsSpinners = new Runnable() {
        public void run() {
            //Spinner Estado
            state = (Spinner) findViewById(R.id.spnStateDB);
            state.setAdapter(dataAdapterUf);
            //Spinner Classe
            type = (Spinner) findViewById(R.id.spnClassDB);
            type.setAdapter(dataAdapterType);
            //Spinner Context
            content = (Spinner) findViewById(R.id.spnContexDBt);
            content.setAdapter(dataAdapterContext);

            AppManager.showView(self, R.id.btnUpdate);
            AppManager.hideView(self, R.id.sync_loading_db);
        }
    };

    final Runnable mtResultsSpinnersSelected = new Runnable() {
        public void run() {
            if(ufSelected != null && classSelected == null) {
                //Spinner Classe
                type = (Spinner) findViewById(R.id.spnClassDB);
                type.setAdapter(dataAdapterType);
                type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        classSelected = parentView.getItemAtPosition(position).toString();

                        if (!classSelected.equals("Selecione Classe")) {

                            AppManager.hideView(self, R.id.btnUpdate);
                            AppManager.showView(self, R.id.sync_loading_db);

                            String whereClause = "uf='" + ufSelected + "' and classe='"+classSelected+ "'";
                            try {
                                loadSipnnerWhereClause("contexto", whereClause);
                            }catch (Exception error){

                            }
                        }

                        if(content!= null){
                            content.clearAnimation();
                            content.setSelection(0);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            }

            if(ufSelected != null && classSelected != null) {
                //Spinner Context
                content = (Spinner) findViewById(R.id.spnContexDBt);
                content.setAdapter(dataAdapterContext);
            }

            AppManager.showView(self, R.id.btnUpdate);
            AppManager.hideView(self, R.id.sync_loading_db);
        }
    };

    final Runnable mtResultsSpinnersUF = new Runnable() {
        public void run() {
            //Spinner Estado
            state = (Spinner) findViewById(R.id.spnStateDB);
            state.setAdapter(dataAdapterUf);
            state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    ufSelected = parentView.getItemAtPosition(position).toString();
                    classSelected = null;

                    if(!ufSelected.equals("Selecione Estado") && ufSelected != null){
                        String whereClause = "uf='" + ufSelected + "'";
                        try {
                            loadSipnnerWhereClause("classe", whereClause);
                        }catch (Exception error){

                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });

            type = (Spinner) findViewById(R.id.spnClassDB);
            type.setAdapter(dataAdapterType);

            content = (Spinner) findViewById(R.id.spnContexDBt);
            content.setAdapter(dataAdapterContext);

            AppManager.showView(self, R.id.btnUpdate);
            AppManager.hideView(self, R.id.sync_loading_db);
        }
    };

    /**
     * Carrega os itens nos Spinners
     */
    private void loadSipnner(){
        try {
            getDataSpinners();
        }
        catch (Exception error){

        }
    }

    /**
     * Carrega os itens nos Spinners
     */
    private void loadSipnnerUF(){
        try {
            getDataSpinnersUF();
        }
        catch (Exception error){

        }
    }

    private void loadSipnnerWhereClause(String column, String whereClause){
        try {
            getDataSpinnersWhereClause(column, whereClause);
        }
        catch (Exception error){

        }
    }

    private void getDataSpinnersWhereClause(final String column, final String whereClause){
        new Thread(){
            public void run(){
                String answer = null;
                try {
                    answer = HttpConnection.getDataSpinnersWhereClause("http://74.208.229.211:8080/RotasBR/rest/json/groupBy", column, whereClause);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(answer != "") {
                    degenerateJSONSpinnersWhereClause(answer);
                    mHandlerSpinnersSelected.post(mtResultsSpinnersSelected);
                }
            }
        }.start();
    }

    private void getDataSpinnersUF(){
        new Thread(){
            public void run(){
                String answerUf = null;
                String answerType = null;
                String answerContext = null;
                try {
                    answerUf = HttpConnection.getDataSpinners("http://74.208.229.211:8080/RotasBR/rest/json/groupBy", "uf");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(answerUf != "" || answerType != "" || answerContext != "") {
                    degenerateJSONSpinnersUF(answerUf, answerType, answerContext);
                    mHandlerSpinnersUF.post(mtResultsSpinnersUF);
                }
            }
        }.start();
    }

    private void getDataSpinners(){
        new Thread(){
            public void run(){
                String answerUf = null;
                String answerType = null;
                String answerContext = null;
                try {
                    answerUf = HttpConnection.getDataSpinners("http://74.208.229.211:8080/RotasBR/rest/json/groupBy", "uf");
                    answerType = HttpConnection.getDataSpinners("http://74.208.229.211:8080/RotasBR/rest/json/groupBy", "classe");
                    answerContext = HttpConnection.getDataSpinners("http://74.208.229.211:8080/RotasBR/rest/json/groupBy", "contexto");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(answerUf != "" || answerType != "" || answerContext != "") {
                    degenerateJSONSpinners(answerUf, answerType, answerContext);
                    mHandlerSpinners.post(mtResultsSpinners);
                }
            }
        }.start();
    }

    public void degenerateJSONSpinnersWhereClause(String data){
        try
        {
            //loads Data 1
            JSONArray valuesColumn = new JSONArray(data);
            JSONObject joColumn;
            namesSpinners = new String[valuesColumn.length() + 1];

            //loads Data 2
            if(ufSelected != null && classSelected == null) {
                valuesColumn = new JSONArray(data);
                namesSpinners = new String[valuesColumn.length() + 1];

                namesSpinners[0] = "Selecione Classe";

                if (valuesColumn.length() > 0) {
                    for (int tam = 0; tam < valuesColumn.length(); tam++) {
                        joColumn = valuesColumn.getJSONObject(tam);
                        namesSpinners[tam + 1] = joColumn.getString("classe");
                        ; //create array of name
                    }

                    // Creating adapter for spinner
                    dataAdapterType = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinners);

                    // Drop down layout style - list view with radio button
                    dataAdapterType
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                }
            }
            //loads Data 3
            if(ufSelected != null && classSelected != null) {
                valuesColumn = new JSONArray(data);
                namesSpinners = new String[valuesColumn.length() + 1];

                namesSpinners[0] = "Selecione Contexto";

                if (valuesColumn.length() > 0) {
                    for (int tam = 0; tam < valuesColumn.length(); tam++) {
                        joColumn = valuesColumn.getJSONObject(tam);
                        namesSpinners[tam + 1] = joColumn.getString("contexto");
                        ; //create array of name
                    }

                    // Creating adapter for spinner
                    dataAdapterContext = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinners);

                    // Drop down layout style - list view with radio button
                    dataAdapterContext
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                }
            }

        }
        catch (JSONException e){ e.printStackTrace(); }
    }

    public void degenerateJSONSpinnersUF(String data1, String data2, String data3){
        try
        {
            //loads Data 1
            JSONArray valuesColumn = new JSONArray(data1);
            JSONObject joColumn;
            namesSpinners = new String[valuesColumn.length() + 1];

            namesSpinners[0] = "Selecione Estado";

            if(valuesColumn.length() > 0) {
                for(int tam = 0; tam < valuesColumn.length(); tam++){
                    joColumn = valuesColumn.getJSONObject(tam);
                    namesSpinners[tam + 1] = joColumn.getString("uf");; //create array of name
                }

                // Creating adapter for spinner
                dataAdapterUf = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, namesSpinners);

                // Drop down layout style - list view with radio button
                dataAdapterUf
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }

            //loads Data 2
            try {
                namesSpinners = new String[1];
                namesSpinners[0] = "Selecione Classe";
                // Creating adapter for spinner
                dataAdapterType = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, namesSpinners);

                // Drop down layout style - list view with radio button
                dataAdapterType
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            }catch (Exception error) {
                Utils.showError(this, error.getMessage());
            }
            //loads Data 3
            namesSpinners = new String[1];
            namesSpinners[0] = "Selecione Contexto";
            // Creating adapter for spinner
            dataAdapterContext = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, namesSpinners);
            // Drop down layout style - list view with radio button
            dataAdapterContext
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        }
        catch (JSONException e){ e.printStackTrace(); }
    }

    public void degenerateJSONSpinners(String data1, String data2, String data3){
        try
        {
            //loads Data 1
            JSONArray valuesColumn = new JSONArray(data1);
            JSONObject joColumn;
            namesSpinners = new String[valuesColumn.length() + 1];

            namesSpinners[0] = "Selecione Estado";

            if(valuesColumn.length() > 0) {
                for(int tam = 0; tam < valuesColumn.length(); tam++){
                    joColumn = valuesColumn.getJSONObject(tam);
                    namesSpinners[tam + 1] = joColumn.getString("uf");; //create array of name
                }

                // Creating adapter for spinner
                dataAdapterUf = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, namesSpinners);

                // Drop down layout style - list view with radio button
                dataAdapterUf
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }


            //loads Data 2
            valuesColumn = new JSONArray(data2);
            namesSpinners = new String[valuesColumn.length() + 1];

            namesSpinners[0] = "Selecione Classe";

            if(valuesColumn.length() > 0) {
                for(int tam = 0; tam < valuesColumn.length(); tam++){
                    joColumn = valuesColumn.getJSONObject(tam);
                    namesSpinners[tam + 1] = joColumn.getString("classe");; //create array of name
                }

                // Creating adapter for spinner
               dataAdapterType = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, namesSpinners);

                // Drop down layout style - list view with radio button
                dataAdapterType
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }

            //loads Data 3
            valuesColumn = new JSONArray(data3);
            namesSpinners = new String[valuesColumn.length() + 1];

            namesSpinners[0] = "Selecione Contexto";

            if(valuesColumn.length() > 0) {
                for(int tam = 0; tam < valuesColumn.length(); tam++){
                    joColumn = valuesColumn.getJSONObject(tam);
                    namesSpinners[tam + 1] = joColumn.getString("contexto");; //create array of name
                }

                // Creating adapter for spinner
                dataAdapterContext = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, namesSpinners);

                // Drop down layout style - list view with radio button
                dataAdapterContext
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }

        }
        catch (JSONException e){ e.printStackTrace(); }
    }
}
