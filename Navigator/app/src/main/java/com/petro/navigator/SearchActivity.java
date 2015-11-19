package com.petro.navigator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

    // ArrayList for Listview
    ArrayList<HashMap<String, String>> productList;

    /**
     * Construtor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Inicialização padrão do construtor
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Controle list view onde os itens serão renderizados
        listView = (ListView) findViewById(R.id.list_view);

        // Preenche a lista com os itens do banco de dados
        // Acessado via variável global all
        fill(AppManager.location.all());

        // Seta o listener para o campo de texto
        searchText = (EditText) findViewById(R.id.inputSearch);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

                // Ao digitar o texto, um like é executado no banco de dados, e o resultado é renderizado na lista
                fill(AppManager.location.like(charSequence.toString()));
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Renderiza os itens no listview
     * @param locations Localidades a serem renderizadas
     */
    private void fill(final List<LocationModel> locations){

        // Itens a serem renderizados
        String items[] = new String[locations.size()];

        // Insere os itens da lista no arry
        for (LocationModel location : locations)
            items[locations.indexOf(location)] = location.getDesc();

        // Limpa a lista
        listView.setAdapter(null);

        // Cria o adapter para a lista
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_detail, R.id.product_name, items);

        // Atualiza a lista  e informa um callback para cada item clicado nela
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                // Pega a locaidade, seta o item como selecionado e retorna
                LocationModel location = locations.get(position);

                listView.setItemChecked(position, true);

                sendData(location);
            }
        });
    }

    /**
     * Ensia os dadide voltaiara a actibiity principal Mainictiviti
     * @param location A locaidade seliionadai
     */
    private void sendData(LocationModel location){

        // iia a intei, seta resultado e finaliza a viewi
        Intent output = new Intent();
        output.putExtra(MainActivity.SEARCH_DATA, location.getDesc() );
        setResult(RESULT_OK, output);
        finish();
    }
}
