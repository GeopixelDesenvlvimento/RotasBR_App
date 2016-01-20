package com.petro.navigator;

import android.app.LauncherActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.petro.navigator.controller.Location;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 *
 * Activity de consulta de localidades
 *
 */
public class SearchActivity extends AppCompatActivity {

    // Variáveis
    // Constroles
    private ListView listView;
    private EditText searchText;
    private Integer oldPosition = -1;
    LocationModel locationPosition = null;

    // ArrayList for Listview
    ArrayList<HashMap<String, String>> productList;

    /**
     * Construtor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            // Inicialização padrão do construtor
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            String stateValue = this.getIntent().getStringExtra("sValue");
            String typeValue = this.getIntent().getStringExtra("tValue");
            String contentValue = this.getIntent().getStringExtra("cValue");
            String idValue = this.getIntent().getStringExtra("eValue");

            // Controle list view onde os itens serão renderizados
            listView = (ListView) findViewById(R.id.list_view);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            listView.setSelector(R.drawable.my_selecter);

            AppManager.app = this;
            if(AppManager.location == null)
                AppManager.location = new Location(this, false);

            if (AppManager.positionListFrom != null) {
                CheckBox chkFrom = (CheckBox) findViewById(R.id.chkFrom);//Origem
                chkFrom.setChecked(true);
            }

            // Preenche a lista com os itens do banco de dados
            // Acessado via variável global all
            if ((stateValue != null && !stateValue.isEmpty()) || (typeValue != null && !typeValue.isEmpty()) || (contentValue != null && !contentValue.isEmpty()) || (idValue != null && !idValue.isEmpty())) {
                fill(AppManager.location.likeMoreFields(stateValue, typeValue, contentValue, idValue));
            } else {
                fill(AppManager.location.all());
            }

            // Seta o listener para o campo de texto
            searchText = (EditText) findViewById(R.id.inputSearch);
            searchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

                    // Ao digitar o texto, um like é executado no banco de dados, e o resultado é renderizado na lista
                    fill(AppManager.location.like(charSequence.toString()));
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }

    /**
     * Renderiza os itens no listview
     * @param locations Localidades a serem renderizadas
     */
    private void fill(final List<LocationModel> locations){

        try {
            // Itens a serem renderizados
            String items[] = new String[locations.size()];

            //Se os retorno da lista de POI'S for maior que 1 então exibe o CheckBox para 'Selecionar Todos'
            if (locations.size() > 1) {
                CheckBox chkAll = (CheckBox) findViewById(R.id.chkAll);
                chkAll.setVisibility(View.VISIBLE);

                CheckBox chkFrom = (CheckBox) findViewById(R.id.chkFrom);
                chkFrom.setVisibility(View.VISIBLE);

                CheckBox chkTo = (CheckBox) findViewById(R.id.chkTo);
                chkTo.setVisibility(View.VISIBLE);

                AppManager.locationsListView = locations;
            } else if (locations.size() == 1) {
                // Pega a locaidade, seta o item como selecionado e retorna
                LocationModel location = locations.get(0);
                AppManager.location.addMarkerMap(location);
                AppManager.locationsListView = locations;
                sendData(location);
            }

            // Insere os itens da lista no arry
            for (LocationModel location : locations) {
                items[locations.indexOf(location)] = location.getDesc();
                //AppManager.location.addMarkerMap(location);
            }

            // Limpa a lista
            listView.setAdapter(null);

            // Cria o adapter para a lista
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_detail, R.id.product_name, items);

            // Atualiza a lista  e informa um callback para cada item clicado nela
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                    CheckBox chkFrom = (CheckBox) findViewById(R.id.chkFrom);//Origem
                    CheckBox chkTo = (CheckBox) findViewById(R.id.chkTo);//Destino
                    // Pega a locaidade, seta o item como selecionado e retorna
                    LocationModel location = locations.get(position);
                    listView.setItemChecked(position, true);

                    //Verifica se o checkbox de Origem está checado
                    if (chkFrom.isChecked()) {
                        if (AppManager.positionListFrom == null) {
                            //caso esteja e a posição de origem seja nula, ele adiciona a origem e avisa o usuário pra escolher um destino.
                            AppManager.positionListFrom = location;
                            Utils.showAlert(SearchActivity.this, "Por favor, selecione um destino");
                        } else {
                            //adiciona uma posição de destino e envia o trajeto para cálculo de navegação.
                            AppManager.positionListTo = location;
                            AppManager.location.addMarkerMap(AppManager.positionListFrom);
                            AppManager.location.addMarkerMap(location);
                            sendData(location);
                        }
                    } else {
                        AppManager.positionListTo = null;
                        AppManager.positionListFrom = null;
                        AppManager.location.addMarkerMap(location);
                        sendData(location);
                    }
                }
            });
        }catch (Exception error){
            Utils.showError(this, error.getMessage());
        }
    }

    /**
     * Ensia os dadide voltaiara a actibiity principal Mainictiviti
     * @param location A locaidade seliionadai
     */
    private void sendData(LocationModel location){

        try {
            // iia a intei, seta resultado e finaliza a viewi
            Intent output = new Intent();
            output.putExtra(MainActivity.SEARCH_DATA, location.getDesc() );
            setResult(RESULT_OK, output);
            finish();
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }

    }

    public void itemClicked(View v) {
        try {
            //code to check if this checkbox is checked!
            CheckBox checkBox = (CheckBox) v;
            if (checkBox.isChecked()) {
                // iia a intei, seta resultado e finaliza a viewi
                AppManager.searcAllListItens = true;
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("all", "true");
                startActivity(intent);
            }
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }

    public void itemFromClicked(View v) {
        try {
            //code to check if this checkbox is checked!
            CheckBox checkBoxFrom = (CheckBox) v;//Origem
            CheckBox chkTo = (CheckBox) findViewById(R.id.chkTo);//Destino
            CheckBox chkAll = (CheckBox) findViewById(R.id.chkAll);//Selecionar Todos

            if (checkBoxFrom.isChecked()) {
                chkTo.setChecked(false);
                chkAll.setEnabled(false);
            }else{
                chkAll.setEnabled(true);
                chkTo.setChecked(true);

                AppManager.positionListTo = null;
                AppManager.positionListFrom = null;
            }

        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
