package com.petro.navigator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.petro.navigator.controller.Location;
import com.petro.navigator.dao.LocationDAO;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddPoiActivity extends AppCompatActivity {

    Spinner state;
    Spinner type;
    Spinner content;
    EditText eText;

    private String ufSelected = null;
    private String classSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //Seta Title
        setTitle("Adicionar Ponto Petrobras");
        //Carregando os Spinners da Activity
        loadSipnner("", "uf");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Evento de clique do botão Cancelar Pois
     */
    public void CancelAddPoi(View v) throws IOException {

        try {
            Intent intent = new Intent();
            intent.setClass(AddPoiActivity.this, MainActivity.class);
            startActivity(intent);
            AppManager.setState(AppManager.STATE.Waiting);
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }

    /**
     * Evento de clique do botão Adicionar Pois
     */
    public void AddPoi(View v) throws IOException {

        try {

            if(AppManager.positionPress != null){

                String stateValue = "Selecione Estado";
                String typeValue = "Selecione Classe";
                String contentValue = "Selecione Contexto";

                Spinner stateS = (Spinner) findViewById(R.id.spinnerAdd);
                if(stateS.getSelectedItem() != null) {
                    stateValue = stateS.getSelectedItem().toString();
                }

                Spinner stateT = (Spinner) findViewById(R.id.spinner2Add);
                if(stateT.getSelectedItem() != null) {
                    typeValue = stateT.getSelectedItem().toString();
                }

                Spinner stateC = (Spinner) findViewById(R.id.spinner3Add);
                if(stateC.getSelectedItem() != null) {
                    contentValue = stateC.getSelectedItem().toString();
                }

                EditText edText = (EditText) findViewById(R.id.eTIDAdd);
                String edTextValue = edText.getText().toString();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String currentDate = sdf.format(new Date());
                //String val = (String)dateNow.getYear();

                List<LocationModel> points = new ArrayList<LocationModel>();
                points.add(new LocationModel(stateValue, typeValue, contentValue, edTextValue, edTextValue,  Integer.parseInt(currentDate),(float)AppManager.positionPress.getLongitude(),
                        (float)AppManager.positionPress.getLatitude()));

                // Inicializa o DAO
                LocationDAO dao = new LocationDAO(AddPoiActivity.this);

                // Para cada ponto, insere no mapa e no banco
                for (LocationModel point : points) {
                    dao.insert(point.getUf(), point.getType(), point.getContext(), point.getTitle(), point.getDesc(), point.getDate(), point.getLon(), point.getLat() );
                    AppManager.positionAddPress = point;
                }
            }
            Utils.showSuccesAddPoint(AddPoiActivity.this, "Ponto Adicionado com Sucesso!");
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
                AppManager.location = new Location(AddPoiActivity.this, false);

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
                    state = (Spinner) findViewById(R.id.spinnerAdd);
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
                    type = (Spinner) findViewById(R.id.spinner2Add);
                    type.setAdapter(dataAdapterTypeOneField);


                    namesSpinnersUF = new String[1];
                    namesSpinnersUF[0] = "Selecione Contexto";
                    //Spinner Contexto
                    ArrayAdapter<String> dataAdapterContextOneField = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, namesSpinnersUF);

                    // Drop down layout style - list view with radio button
                    dataAdapterContextOneField
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    content = (Spinner) findViewById(R.id.spinner3Add);
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
                    type = (Spinner) findViewById(R.id.spinner2Add);
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
                    content = (Spinner) findViewById(R.id.spinner3Add);

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
