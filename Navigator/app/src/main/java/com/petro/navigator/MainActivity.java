package com.petro.navigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.guidance.VoicePackage;
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
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Método de implementação
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // search for the map fragment to finish setup by calling init().
            AppManager.app = this;
            AppManager.setState(AppManager.STATE.Waiting);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

            //Liga GPS
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            if (!provider.contains("gps")) { //if gps is disabled
                Utils.showAlert(this, "O GPS está desabilitado, para funcionamento correto do aplicativo por favor ligue-o!");
            }

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
                        AppManager.app = MainActivity.this;
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

                        if(AppManager.positionAddPress != null){
                            AppManager.location.addMarkerMap(AppManager.positionAddPress);
                            AppManager.positionAddPress = null;
                        }

                        //Se os retorno da lista de POI'S for maior que 1 então exibe o CheckBox para 'Selecionar Todos'
                        List<LocationModel> locationsListView = AppManager.locationsListView;
                        if (locationsListView != null) {

                            if (locationsListView.size() > 0 && AppManager.searcAllListItens == true) {
                                // Insere os itens da lista no arry
                                for (LocationModel location : locationsListView) {
                                    AppManager.location.zoomTo(location);
                                    AppManager.location.addMarkerMap(location);
                                    location.getMarker().showInfoBubble();
                                }

                                AppManager.searcAllListItens = false;
                                AppManager.map.setZoomLevel((AppManager.map.getMaxZoomLevel() + AppManager.map.getMinZoomLevel()) / 3);
                            }
                        }
                    } else
                        Utils.showError(MainActivity.this, "Não foi possível carregar o mapa!");
                    downloadVoiceCatalog();
                }
            });
        }
        catch (Exception error)
        {
            //Utils.showError(this, error.getMessage());
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();

            if (AppManager.positioningManager != null)
                AppManager.positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
        }
        catch (Exception error)
        {
          //  Utils.showError(this, error.getMessage());
        }
    }

    @Override
    public void onPause(){
        try {
            super.onPause();

            if (AppManager.positioningManager != null)
                AppManager.positioningManager.stop();

            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "no sleep");
            wakeLock.release();

            MapEngine.getInstance().onPause();

            System.out.println(">>>>>>>>>>> PAUSE");
        }
        catch (Exception error)
        {
            //Utils.showError(this, error.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == SEARCH_DATA_CODE && resultCode == RESULT_OK && data != null) {

                String desc = data.getStringExtra(this.SEARCH_DATA);
                LocationModel location = AppManager.location.get(desc);

                AppManager.location.zoomTo(location);
                AppManager.location.set(location);
                AppManager.setState(AppManager.STATE.Waiting);
            }
        }catch (Exception error)
        {
            //Utils.showError(this, error.getMessage());
        }
    }

    @Override
    public void onBackPressed() {

        if(AppManager.getState() == AppManager.STATE.Navigating || AppManager.getState() == AppManager.STATE.Routing) {
            AppManager.setState(AppManager.STATE.Waiting); // state
            AppManager.navigationManager.stop();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            super.onBackPressed();
        } else {
            Utils.showExit(this);
        }

        /*try {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
            return false;
        }
    }

    /**
     * Seleção de itens da toolbar
     * @param item Item selecionado
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            if (id == R.id.action_sync) {

                // Valida se existe uma conexão de internet ativa, e só redireciona para o sync manager caso exista.
                if (Utils.isOnline()) {

                    Intent syncActivityIntent = new Intent(this, SyncActivity.class);
                    startActivity(syncActivityIntent);
                } else
                    Utils.showAlert(this, "E necessario ter uma conexão de internet ativa!");

                AppManager.setState(AppManager.STATE.Sync);
                return false;
            } else if (id == R.id.action_search) {

                Intent searchActivityIntent = new Intent(this, SearchActivity.class);
                startActivityForResult(searchActivityIntent, SEARCH_DATA_CODE);

                AppManager.setState(AppManager.STATE.Searching);
                return false;
            } else if (id == R.id.action_gps) {
                if(AppManager.map != null) {
                    AppManager.map.setOrientation(0, Map.Animation.LINEAR);
                }

                AppManager.position.zoomToCurrentPosition();
            }

            return super.onOptionsItemSelected(item);
        }catch (Exception error){
            //Utils.showError(this, error.getMessage());
            return  false;
        }
    }

    /**
     * Seleção de itens do drawer
     * @param item Item selecionado
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        try {
            int id = item.getItemId();

            if (id == R.id.action_sync) {
                // Valida se existe uma conexão de internet ativa, e só redireciona para o sync manager caso exista.
                if (Utils.isOnline()) {

                    Intent syncActivityIntent = new Intent(this, SyncActivity.class);
                    startActivity(syncActivityIntent);
                } else
                    Utils.showAlert(this, "E necessario ter uma conexão de internet ativa!");

                AppManager.setState(AppManager.STATE.Sync);
                return false;
            } else if (id == R.id.nav_sync) {
                Intent syncDBActivityIntent = new Intent(this, SyncDBActivity.class);
                startActivity(syncDBActivityIntent);

            } else if (id == R.id.nav_exit) {
                Utils.showExit(this);
                //android.os.Process.killProcess(android.os.Process.myPid());
            } else if (id == R.id.nav_search_pois) {

                Intent poiActivityIntent = new Intent(this, PoiActivity.class);
                startActivity(poiActivityIntent);
            }else if (id == R.id.nav_search_pois_here) {
                Utils.showAlert(this, "Funcionalidade desabilitada"); //TODO:Adicionar código que irá chamar as outras páginas no futuro
            }else if (id == R.id.create_pois_favs) {
                Utils.showAlert(this, "Funcionalidade desabilitada"); //TODO:Adicionar código que irá chamar as outras páginas no futuro
            }else if (id == R.id.nav_search_pois_favs) {
                Utils.showAlert(this, "Funcionalidade desabilitada"); //TODO:Adicionar código que irá chamar as outras páginas no futuro
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }catch (Exception error) {
            //Utils.showError(this, error.getMessage());
            return true;
        }
    }

    private void downloadVoiceCatalog() {
        boolean result = VoiceCatalog.getInstance().downloadCatalog(new VoiceCatalog.OnDownloadDoneListener(){
            @Override
            public void onDownloadDone(VoiceCatalog.Error error) {
                //Toast.makeText(getApplicationContext(), "onDownloadDone: " + error.toString(), Toast.LENGTH_LONG).show();

                // Get the list of voice packages from the voice catalog list
                List<VoicePackage> voicePackages = VoiceCatalog.getInstance().getCatalogList();
                long id = -1;
                // select
                for (VoicePackage pacote : voicePackages) {
                    String language = pacote.getMarcCode();
                    if (language.compareToIgnoreCase("por") == 0) {
                        if (pacote.isTts()) {
                            id = pacote.getId();
                            break;
                        }
                    }
                }
                try {
                    if (!VoiceCatalog.getInstance().isLocalVoiceSkin(id)) {
                        final long finalId = id;
                        VoiceCatalog.getInstance().downloadVoice(id, new VoiceCatalog.OnDownloadDoneListener() {
                            @Override
                            public void onDownloadDone(VoiceCatalog.Error error) {
                                if (error == VoiceCatalog.Error.NONE) {
                                    //voice skin download successful

                                    // set the voice skin for use by navigation manager
                                    if (VoiceCatalog.getInstance().getLocalVoiceSkin(finalId) != null) {
                                        AppManager.navigationManager.setVoiceSkin(VoiceCatalog.getInstance().getLocalVoiceSkin(finalId));
                                    } else {
                                        //Toast.makeText(mActivity.getApplicationContext(), "Navi manager set voice skin error.", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    //Toast.makeText(mActivity.getApplicationContext(), "Voice skin download error.", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                    } else {
                        // set the voice skin for use by navigation manager
                        if (VoiceCatalog.getInstance().getLocalVoiceSkin(id) != null) {
                            AppManager.navigationManager.setVoiceSkin(VoiceCatalog.getInstance().getLocalVoiceSkin(id));
                        } else {

                            //Toast.makeText(mActivity.getApplicationContext(), "Navi manager set voice skin error.", Toast.LENGTH_LONG).show();
                        }
                    }

                }catch (Exception errordd)
                {
                    Utils.showError(null, "sdf");
                }

            }});
    }
}
