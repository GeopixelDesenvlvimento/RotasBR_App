package com.petro.navigator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.odml.MapLoader;
import com.petro.navigator.controller.Location;
import com.petro.navigator.controller.Navigation;
import com.petro.navigator.controller.Position;
import com.petro.navigator.controller.Routing;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import java.io.IOException;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.*;

/**
 *
 * Activity principal do sistema, onde o mapa é renderizado e todas os fragmentos também
 *
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Variáveis estáticas para gestão da consulta
    public final static int SEARCH_DATA_CODE = 13;
    public final static String SEARCH_DATA = "com.petro.navigator.SearchActivity";
    //private AlertDialog alerta;

    /**
     * Método de implementação
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // search for the map fragment to finish setup by calling init().
        AppManager.app = this;
        AppManager.setState(AppManager.STATE.Waiting);

        //Inicializa classes de gestão para esconder fragmentos
        new Navigation(false).hideManeuver();
        new Navigation(false).hideStats();
        new Routing(false).hideDetail();

        // Inicializa toolbar e drawer lateral
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Inicializa a navigation view e o fragmento de mapa
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AppManager.mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        AppManager.mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {

                // Valida disponibilidade da internet, e exibe ou omite o botão de sincronismo
                if (Utils.isOnline())
                    AppManager.showView(R.id.action_sync);
                else
                    AppManager.hideView(R.id.action_sync);

                // Caso o mapa tenha sido carregado com sucesso com ou sem conexão de internet, alguns elementos são inicializados
                if (error == OnEngineInitListener.Error.NONE) {

                    // Inicializa o map loader, responsável pela gestão de pacotes de mapa
                    AppManager.mapLoader = MapLoader.getInstance();

                    // Inicializa o mapa e os atributos de mapa
                    AppManager.map = AppManager.mapFragment.getMap();
                    AppManager.map.setTrafficInfoVisible(true);
                    AppManager.map.setMapScheme(Map.Scheme.NORMAL_DAY);

                    // Insere uma imagem padrão para o indicador de posição padrão do mapa
                    try {
                        Image image = new Image();
                        image.setImageResource(R.drawable.gps64);
                        AppManager.map.getPositionIndicator().setMarker(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Instância as classes de gestão
                    AppManager.position = new Position();
                    AppManager.location = new Location(MainActivity.this, true);
                    AppManager.routing = new Routing(true);
                    AppManager.navigation = new Navigation(true);

                } else
                    Utils.showError(MainActivity.this, "Não foi possível carregar o mapa!");

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (AppManager.positioningManager != null)
            AppManager.positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
    }

    @Override
    public void onPause(){
        super.onPause();

        if (AppManager.positioningManager != null)
            AppManager.positioningManager.stop();

        MapEngine.getInstance().onPause();

        System.out.println(">>>>>>>>>>> PAUSE");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEARCH_DATA_CODE && resultCode == RESULT_OK && data != null) {

            String desc = data.getStringExtra(this.SEARCH_DATA);
            LocationModel location = AppManager.location.get(desc);

            AppManager.location.zoomTo(location);
            AppManager.location.set(location);
            AppManager.setState(AppManager.STATE.Waiting);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Seleção de itens da toolbar
     * @param item Item selecionado
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sync) {

            // Valida se existe uma conexão de internet ativa, e só redireciona para o sync manager caso exista.
            if (Utils.isOnline()) {

                Intent syncActivityIntent = new Intent(this, SyncActivity.class);
                startActivity(syncActivityIntent);
            }
            else
                Utils.showAlert(this, "E necessario ter uma conexao de internet ativa!");

            AppManager.setState(AppManager.STATE.Sync);

            return false;
        }
        else if (id == R.id.action_search) {

            Intent searchActivityIntent = new Intent(this, SearchActivity.class);
            startActivityForResult(searchActivityIntent, SEARCH_DATA_CODE);

            AppManager.setState(AppManager.STATE.Searching);

            return false;
        }
        else if (id == R.id.action_gps){
            AppManager.position.zoomToCurrentPosition();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Seleção de itens do drawer
     * @param item Item selecionado
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_sync) {
            Toast.makeText(this, "NÃO IMPLEMENTADO", Toast.LENGTH_SHORT);
        } else if (id == R.id.nav_exit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (id == R.id.nav_search_pois) {

            Intent poiActivityIntent = new Intent(this, PoiActivity.class);
            startActivity(poiActivityIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
