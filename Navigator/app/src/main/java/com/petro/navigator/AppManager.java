package com.petro.navigator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.odml.MapLoader;
import com.here.android.mpa.odml.MapPackage;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteResult;
import com.petro.navigator.controller.Location;
import com.petro.navigator.controller.Navigation;
import com.petro.navigator.controller.Position;
import com.petro.navigator.controller.Routing;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Módulo estático de gerenciamento da aplicaçaõ *
 *
 * Este módulo faz o gerenciamento de todas as funções que são comuns entre as views do sistema.
 * Também mantém uma instância singleton das classes principais de localização, navegação, roteireirização e usuário.
 *
 */
public class AppManager {

    public static AppCompatActivity app;

    // elementos publicos relacionados ao mapa
    public static Map map = null; // objeto mapa sdk here
    public static MapFragment mapFragment = null; // fragmento do mapa da activity principal
    public static MapLoader mapLoader  = null; // loader é responsável pelo gestão dos pacotes de mapa offline
    public static List<LocationModel> locationsListView = null; // lista de locais da pesquisa de POIS.

    // Localizaçao: elementos relacionados
    public static Location location; // classe de gestão das localizações (POIs)
    public static MapMarker selectedLocation; // localização (POI) selecionada

    // Navegação: elementos relacionados
    public static Navigation navigation; // classe de gestão e operação das features de navegação (turn-by-turn). start, stop, reroute, etc.
    public static NavigationManager navigationManager; // lib da HERE de navegação, utilizada na classe controller.Navigation

    public static AudioManager audioManager; // lib da HERE de navegação, utilizada na classe controller.Navigation

    // Roteirização: elementos relacionados
    public static Routing routing; // classe de gestão e operação das features de roteirização (routing, travel-time, distance, etc)
    public static RouteManager routeManager; // Lib da HERE de roteirização, utilizada na classe ontroller.Routing
    public static RouteResult routeResult; // variável que armazena o resultado de uma roteirização
    public static MapRoute routeGeometry; // variável que armazena a geometria do resultado de uma roteirização
    public static ProgressBar routeLoading; // elemento de loading da roteirização

    // Posicionamento: elementos relacionados
    public static Position position; // classe de gestão do posicionamento do dispositivo
    public static PositioningManager positioningManager; // Lib da HERE de posicionamento, utilizada para menter o posicionamento do dispositivo atualizado.
    public static GeoCoordinate currentPosition; // atual localização dos dispositivo, capturado pela classe PositioningManager
    public static boolean currentPositionZoomTo = false;

    // Sincronismo: elemtnso relacionados
    // Componentes do item selecionado para download dos dados. Componentes não são alterados caso existe um sincronismo em admaaneto
    public static int mapPackegeId;
    public static ImageView mapPackageImg ;
    public static TextView mapPackageTitle ;
    public static TextView mapPackageStatus ;
    public static TextView mapPackageSize;
    public static ProgressBar mapPackagePercentage ;

    // lista de pacotes de mapas disponíveis no dispositivo
    public static List<MapPackage> mapPackages = new ArrayList<MapPackage>();


    // Gestão dos estados da aplicaçao
    public enum STATE{ Waiting, Creating, Navigating, Searching, Routing, Sync, Syncing }
    private static STATE _state;

    public static STATE getState(){ return _state; }
    public static void setState(STATE state){
        _state = state;
        try{ refreshApp(); } catch(Exception e){ Log.e("STATE CHANGE", " Problema ao setar estado."); }
    }

    /**
     * Métodos público para mostrar ou esconder algum item da tela com animação
     * @param id
     */
    public static void showView(Activity context, int id){
        View view = context.findViewById(id);
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });;
    }
    public static void hideView(Activity context,int id) {
        final View view = context.findViewById(id);
        if (view == null) return;
        view.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static void showView(int id){
        showView(app, id);
    }
    public static void hideView(int id) {
        hideView(app, id);
    }

    /**
     * Método responsável por gerenciador os estados dos elementos do sistema de acordo com o estado atual
     */
    private static void refreshApp(){

        if (_state == STATE.Waiting){

            showView(R.id.action_gps);
            showView(R.id.action_search);
        }
        else if (_state == STATE.Searching ||
                 _state == STATE.Creating){

            hideView(R.id.action_gps);
            hideView(R.id.action_search);
        }
        else if (_state == STATE.Routing){

            hideView(R.id.action_gps);
            hideView(R.id.action_search);
        }
        else if (_state == STATE.Navigating){

            hideView(R.id.action_gps);
            hideView(R.id.action_search);

        }
        else if (_state == STATE.Syncing){

            mapPackageImg.setImageResource(R.drawable.hourglass_64);
        }

        // valida o botão de sincronismo caso exista uma conexão ativa de internet,
        // caso contrário, o botão é sempre invisível.
        if (Utils.isOnline()){
            if (_state == STATE.Navigating)
                hideView(R.id.action_sync);
            else
                showView(R.id.action_sync);
        }
        else
            hideView(R.id.action_sync);
    }

    /**
     * Método que mostra o loading do processamento de roteirização
     */
    public static void showRouteLoading(){
        routeLoading.setVisibility(View.VISIBLE);
        routeLoading.animate().alpha(1.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });;
    }

    /**
     * Método que esconde o loading do processamento de roteirização
     */
    public static void hideRouteLoading() {
        routeLoading.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                routeLoading.setVisibility(View.INVISIBLE);
            }
        });
    }
}
