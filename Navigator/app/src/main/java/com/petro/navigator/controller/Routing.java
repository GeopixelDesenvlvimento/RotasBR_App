package com.petro.navigator.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteTta;
import com.petro.navigator.AppManager;
import com.petro.navigator.R;
import com.petro.navigator.misc.Utils;

import java.util.Date;
import java.util.List;

/**
 *
 * Classe de gerenciamento da roteirização
 */
public class Routing {

    // Variáveis
    // Controles do fragmentos de detalhes da rota
    private View detail;
    private TextView detailTitle;
    private TextView detailTime;
    private TextView detailDistance;
    private TextView detailRoad;
    private TextView detailArrival;

    private View detailNavigation;

    /**
     * Construtor
     * @param initHereLibs Inicializador das libs da here
     */
    public Routing(boolean initHereLibs) {
        if (initHereLibs) {

            // Inicializa o route manager
            if (AppManager.routeManager == null) {
                AppManager.routeManager = new RouteManager();
                AppManager.routeManager.setTrafficPenaltyMode(Route.TrafficPenaltyMode.AVOID_CONGESTION);
            }
        }

        // Inicializa os componentes de interface com o usuário
        initUI();
    }

    /**
     * Inicializa os controles de interface com o usuário (Detalhes)
     */
    public void initUI(){

        // Adiciona ao manager e econde o loading
        AppManager.routeLoading = (ProgressBar)AppManager.app.findViewById(R.id.detail_loading);
        AppManager.hideRouteLoading();

        // Pega os controles dos detlahes da rota
        detail = (View)AppManager.app.findViewById(R.id.detail);
        detailTitle = (TextView) detail.findViewById(R.id.detail_title);
        detailTime = (TextView) detail.findViewById(R.id.detail_time);
        detailDistance = (TextView) detail.findViewById(R.id.detail_distance);
        detailRoad = (TextView) detail.findViewById(R.id.detail_road);
        detailArrival = (TextView) detail.findViewById(R.id.detail_arrival);

        detailNavigation = detail.findViewById(R.id.detail_navigation);

        // Esconde o fragmento detalhes
        hideDetail();

        // Inicializa os listeners
        detailNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // routing
                if (AppManager.currentPosition != null) {

                    // routing

                    hideDetail();
                    AppManager.navigation.start();
                    AppManager.navigation.showManeuver();
                    AppManager.navigation.showStats();

                } else
                    Utils.showAlert(AppManager.app, "Aguarde o sinal do GPS!");
            }
        });

    }

    /**
     * Inicializ o calculo da rota
     * @param from geo coordenada de origem
     * @param to geo coordenada de destino
     */
    public void calc(GeoCoordinate from, GeoCoordinate to){

        //Caso possua coordenadas de Origem e Destino vindas da lista de pesquisa, ele altera o From e To para as novas coordenadas.
        if(AppManager.positionListFrom != null && AppManager.positionListTo != null) {
            from = (GeoCoordinate) AppManager.positionListFrom.getMarker().getCoordinate();
            to = (GeoCoordinate) AppManager.positionListTo.getMarker().getCoordinate();
            AppManager.positionListFrom = null;
            AppManager.positionListTo = null;
        }

        // Remove qualquer geometria de rota que exista no map
        AppManager.map.removeMapObject(AppManager.routeGeometry);

        // Cria as opções da rota, defina o tipo de transporte , a hora de partida e o tipo da rota
        RouteOptions options = new RouteOptions();
        options.setTransportMode(RouteOptions.TransportMode.CAR);
        options.setTime(new Date(), RouteOptions.TimeType.DEPARTURE);
        options.setRouteType(RouteOptions.Type.FASTEST);

        // Adiciona os pontos de origem e destino da rota
        RoutePlan plan = new RoutePlan();
        plan.addWaypoint(from);
        plan.addWaypoint(to);
        plan.setRouteOptions(options);

        // Dispara o calculo da rota, mostra os detalhes e o loading
        AppManager.showRouteLoading();
        AppManager.routing.showDetail();
        AppManager.routeManager.calculateRoute(plan, new RouteListener());

        // Altera o estado da aplicaçao para "roteirizando"
        AppManager.setState(AppManager.STATE.Routing);
    }
    public void calc() { calc(AppManager.currentPosition, AppManager.selectedLocation.getCoordinate()); }

    /**
     * Atualiza os dados provenientes da rota calculada como tempo total da rota, distância total, a via de maior tamanho, etc.
     */
    public void updateDetail(){

        Route route = AppManager.routeResult.getRoute();
        RouteTta routeTta = route.getTta(Route.TrafficPenaltyMode.DISABLED, route.getSublegCount() - 1);

        // updating the text
        detailTitle.setText(AppManager.selectedLocation.getTitle());
        detailTime.setText(Utils.secondsToDateLabel(routeTta.getDuration()));
        detailDistance.setText(Utils.metersToQuilometersLabel(route.getLength(), 1) + " km");
        detailRoad.setText(Utils.roadNameLabel(route.getManeuvers()));
        detailArrival.setText(Utils.secondsPlusNowLabel(routeTta.getDuration()));
    }

    /**
     * Métodos responsáveis por exebir e esconder o fragmento de detalhes da rota
     */
    public void showDetail(){

        if (detail.getVisibility() != View.VISIBLE) {

            detail.setVisibility(View.VISIBLE);
            detail.animate()
                    .translationY(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
        }
    }
    public void hideDetail(){
        if (detail.getVisibility() == View.VISIBLE)

            detail.animate()
                    .translationY(detail.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            detail.setVisibility(View.INVISIBLE);
                        }
                    });
    }

    /**
     * Listener responsável por capturar os eventos relacionados a geraçãod a rota
     */
    private class RouteListener implements RouteManager.Listener {

        // Método necessário para implementação do listener. Não está sendo utilizado
        public void onProgress(int percentage) { }

        // Callback executando quando a rota termina de calcular
        public void onCalculateRouteFinished(RouteManager.Error error, List<RouteResult> routeResult) {

            // Verifica se não existe algum problema ja geraçao da rota
            if (error == RouteManager.Error.NONE) {

                //  Insere no mapa
                AppManager.routeResult = routeResult.get(0);
                AppManager.routeGeometry = new MapRoute(AppManager.routeResult.getRoute());

                //Da zoom no mapa, atuializ os detalhes e esconde o loading
                AppManager.map.addMapObject(AppManager.routeGeometry);
                AppManager.map.zoomTo(AppManager.routeGeometry.getRoute().getBoundingBox(), Map.Animation.LINEAR, 15);
                AppManager.routing.updateDetail();
                AppManager.hideRouteLoading();
            }
            else {
                // Caso exista algum problema, esconde o loading e os detalhes da rota e uma mensagem é exibida
                AppManager.hideRouteLoading();
                AppManager.routing.hideDetail();
                Utils.showError(AppManager.app, "Ocorreu um problema na geração da rota!");
            }

        }
    }
}



