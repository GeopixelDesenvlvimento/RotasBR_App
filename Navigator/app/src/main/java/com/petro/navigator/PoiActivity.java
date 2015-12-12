package com.petro.navigator;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.petro.navigator.controller.Location;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class PoiActivity extends AppCompatActivity {

    // Variáveis estáticas para gestão da consulta
    public final static int SEARCH_DATA_CODE = 13;
    public final static String SEARCH_DATA = "com.petro.navigator.SearchActivity";
    private String ufSelected = null;
    private String classSelected = null;

    Spinner state;
    Spinner type;
    Spinner content;
    EditText eText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_poi);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Seta Titu
            setTitle("Buscar Pois");
            //Carregando os Spinners da Activity
            loadSipnner("", "uf");
        }catch (Exception error){
            Utils.showError(this, error.getMessage());
        }
    }

    /**
     * Evento de clique do botão Buscar Pois
     */
    public void searchPoi(View v) throws IOException {

        try {

            String stateValue = "Selecione Estado";
            String typeValue = "Selecione Classe";
            String contentValue = "Selecione Contexto";

            Spinner stateS = (Spinner) findViewById(R.id.spinner);
            if(stateS.getSelectedItem() != null) {
                stateValue = stateS.getSelectedItem().toString();
            }

            Spinner stateT = (Spinner) findViewById(R.id.spinner2);
            if(stateT.getSelectedItem() != null) {
                typeValue = stateT.getSelectedItem().toString();
            }

            Spinner stateC = (Spinner) findViewById(R.id.spinner3);
            if(stateC.getSelectedItem() != null) {
                contentValue = stateC.getSelectedItem().toString();
            }

            EditText edText = (EditText) findViewById(R.id.eTID);
            String edTextValue = edText.getText().toString();

            Integer sizeList = AppManager.location.likeMoreFieldsCount(stateValue, typeValue, contentValue, edTextValue);

            if (sizeList > 100) {
                Utils.showAlert(this, "Resultado ultrapassa 100 registros, por favor realize uma nova busca com mais filtros");
            } else {
                Intent searchActivityIntent = new Intent(this, SearchActivity.class);
                searchActivityIntent.putExtra("sValue", stateValue);
                searchActivityIntent.putExtra("tValue", typeValue);
                searchActivityIntent.putExtra("cValue", contentValue);
                searchActivityIntent.putExtra("eValue", edTextValue);
                startActivityForResult(searchActivityIntent, SEARCH_DATA_CODE);

                AppManager.setState(AppManager.STATE.Searching);
            }
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if(data != null){
                //Pega a descriação do poi e busca ele na base
                String desc = data.getStringExtra(this.SEARCH_DATA);
                LocationModel location = AppManager.location.get(desc);
                // Seta a localização no mapa e gera a rota
                AppManager.location.zoomTo(location);
                AppManager.location.set(location);
                AppManager.setState(AppManager.STATE.Waiting);
                // Devolve as informações da MainActivity
                Intent output = new Intent();
                output.putExtra(MainActivity.SEARCH_DATA, location.getDesc());
                setResult(RESULT_OK, output);
                finish();
            }
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }

    /**
     * Carrega os itens nos Spinners
     */
    private void loadSipnner(String whereClause, final String typeClass){

        try {
            //Pega o último resgistro do banco de dados TODO:Precisa melhorar o código, está aqui só pra apresentação no momento, mudar na versão final
            if(AppManager.location == null)
                AppManager.location = new Location(PoiActivity.this, false);

            switch (typeClass){
                case "uf":
                    List<LocationModel> listLocationModelsUF = AppManager.location.getValeuGroupBy("UF");
                    String[] namesSpinnersUF = new String[listLocationModelsUF.size() + 1];
                    namesSpinnersUF[0] = "Selecione Estado";
                    for (int i = 0; i < listLocationModelsUF.size(); i++) {
                        namesSpinnersUF[i + 1] = listLocationModelsUF.get(i).getUf(); //create array of name
                    }
                    // Creating adapter for spinner
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinnersUF);

                    // Drop down layout style - list view with radio button
                    dataAdapter
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    //Spinner Estado
                    state = (Spinner) findViewById(R.id.spinner);
                    state.setAdapter(dataAdapter);

                    state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            ufSelected = parentView.getItemAtPosition(position).toString();
                            classSelected = null;
                            if (!ufSelected.equals("Selecione Estado") && ufSelected != null) {
                                if (type != null) {
                                    type.clearAnimation();
                                    type.setSelection(0);
                                }
                                if (content != null) {
                                    content.clearAnimation();
                                    content.setSelection(0);
                                }
                                String whereClauseItem = "uf='" + ufSelected + "'";
                                loadSipnner(whereClauseItem, "classe");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {

                        }
                    });
                    namesSpinnersUF = new String[1];
                    namesSpinnersUF[0] = "Selecione Classe";
                    ArrayAdapter<String> dataAdapterTypeOneField = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinnersUF);

                    // Drop down layout style - list view with radio button
                    dataAdapterTypeOneField
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Spinner Tipo
                    type = (Spinner) findViewById(R.id.spinner2);
                    type.setAdapter(dataAdapterTypeOneField);


                    namesSpinnersUF = new String[1];
                    namesSpinnersUF[0] = "Selecione Contexto";
                    //Spinner Contexto
                    ArrayAdapter<String> dataAdapterContextOneField = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinnersUF);

                    // Drop down layout style - list view with radio button
                    dataAdapterContextOneField
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    content = (Spinner) findViewById(R.id.spinner3);
                    content.setAdapter(dataAdapterContextOneField);

                    break;
                case "classe":
                    List<LocationModel> listLocationModelsType = AppManager.location.WhereClause(whereClause, "type");
                    String[] namesSpinnersType = new String[listLocationModelsType.size() + 1];
                    namesSpinnersType[0] = "Selecione Classe";
                    for (int i = 0; i < listLocationModelsType.size(); i++) {
                        namesSpinnersType[i + 1] = listLocationModelsType.get(i).getType(); //create array of name
                    }
                    ArrayAdapter<String> dataAdapterType = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinnersType);

                    // Drop down layout style - list view with radio button
                    dataAdapterType
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //Spinner Tipo
                    type = (Spinner) findViewById(R.id.spinner2);
                    type.setAdapter(dataAdapterType);

                    type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            classSelected = parentView.getItemAtPosition(position).toString();
                            if (!classSelected.equals("Selecione Classe") && ufSelected != null) {
                                String whereClauseItem = "uf='" + ufSelected + "' and type='" + classSelected + "'";
                                if(content!= null){
                                    content.clearAnimation();
                                    content.setSelection(0);
                                }
                                loadSipnner(whereClauseItem, "contexto");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }
                    });
                    break;
                case "contexto":
                    List<LocationModel> listLocationModelsContext = AppManager.location.WhereClause(whereClause, "context");
                    String[] namesSpinnersContext = new String[listLocationModelsContext.size() + 1];
                    namesSpinnersContext[0] = "Selecione Contexto";
                    for (int i = 0; i < listLocationModelsContext.size(); i++) {
                        namesSpinnersContext[i + 1] = listLocationModelsContext.get(i).getContext(); //create array of name
                    }

                    //Spinner Contexto
                    content = (Spinner) findViewById(R.id.spinner3);

                    ArrayAdapter<String> dataAdapterContext = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinnersContext);

                    // Drop down layout style - list view with radio button
                    dataAdapterContext
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    content.setAdapter(dataAdapterContext);
                    break;
            }

        }catch (Exception error){
            Utils.showError(this, error.getMessage());
        }
    }

}
