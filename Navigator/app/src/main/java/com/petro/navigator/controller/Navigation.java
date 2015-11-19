package com.petro.navigator.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteTta;
import com.petro.navigator.AppManager;
import com.petro.navigator.R;
import com.petro.navigator.misc.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 *
 * Classe de gestão da navegação Turn-by-Turn
 */
public class Navigation {

    // Variáveis
    // Controles do fragmento de estatísticas
    private View navigationStats;
    private TextView navigationSpeed;
    private TextView navigationArrival;
    private TextView navigationMinsTo;

    // Controles do fragmento de manobras
    private View navigationManeuver;
    private View navigationCancel;
    private TextView navigationCurrRoad;
    private TextView navigationNextRoad;
    private TextView navigationNextDistance;
    private ImageView navigationNextImg;

    /**
     * Construtor
     * @param initHereLibs Inicializador das libs da here
     */
    public Navigation(boolean initHereLibs) {
         if(initHereLibs) {

             // Initializa o navigation manager
             if (AppManager.navigationManager == null)
                 AppManager.navigationManager = NavigationManager.getInstance();

             // Inicializa parêmetros da navigation view
             AppManager.navigationManager.setMap(AppManager.map);
             AppManager.navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
             AppManager.navigationManager.setSpeedWarningEnabled(true);

             AppManager.navigationManager.setRealisticViewMode(NavigationManager.RealisticViewMode.DAY);
         }

        // Inicializa os componentes de interface com o usuário
        initUI();
    }

    /**
     * Inicializa os controles de interface com o usuário (Estatísticas e Manobras)
     */
    public void initUI(){

        // Pega os componentes de estatísticas e manobras
        navigationStats = (View)AppManager.app.findViewById(R.id.navigation_stats);
        navigationSpeed = (TextView) navigationStats.findViewById(R.id.navigation_speed);
        navigationArrival = (TextView) navigationStats.findViewById(R.id.navigation_arrival);
        navigationMinsTo = (TextView) navigationStats.findViewById(R.id.navigation_minsto);

        navigationManeuver = (View)AppManager.app.findViewById(R.id.navigation_maneuver);
        navigationCancel = (View) navigationManeuver.findViewById(R.id.navigation_cancel);
        navigationCurrRoad = (TextView) navigationManeuver.findViewById(R.id.navigation_curr_road);
        navigationNextRoad = (TextView) navigationManeuver.findViewById(R.id.navigation_next_road);
        navigationNextDistance = (TextView) navigationManeuver.findViewById(R.id.navigation_next_distance);
        navigationNextImg = (ImageView) navigationManeuver.findViewById(R.id.navigation_next_img);

        // Esconde os fragmentos de estatísticas e manobra
        hideStats();
        hideManeuver();

        // Seta o evento de click no votão para cancelamento da navegação vigente
        navigationCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Cancela a nageçao ae altera o estado da aplicaçao para Waiting, ou seja, esperando uma ação
                AppManager.navigationManager.stop();
                AppManager.setState(AppManager.STATE.Waiting);
            }
        });
    }

    /**
     * Métodos responsáveis por exibir e esconder o fragmento de estatísticas
     */
    public void showStats(){
        if (navigationStats.getVisibility() != View.VISIBLE) {
            navigationStats.setVisibility(View.VISIBLE);
            navigationStats.animate()
                    .translationY(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
        }
    }
    public void hideStats(){
        if (navigationStats.getVisibility() == View.VISIBLE)
            navigationStats.animate()
                    .translationY(navigationStats.getHeight())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            navigationStats.setVisibility(View.INVISIBLE);
                        }
                    });
    }

    /**
     * Métodos responsáveis por exibir e esconder o fragmento de manobras
     */
    public void showManeuver(){
        if (navigationManeuver.getVisibility() != View.VISIBLE) {
            navigationManeuver.setVisibility(View.VISIBLE);
            navigationManeuver.animate()
                    .translationY(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
        }
    }
    public void hideManeuver(){
        if (navigationManeuver.getVisibility() == View.VISIBLE)
            navigationManeuver.animate()
                    .translationY(navigationManeuver.getHeight() * -1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            navigationManeuver.setVisibility(View.INVISIBLE);
                        }
                    });
    }

    /**
     * Inicia a navegação com a rota previamente cadastrada
     */
    public void start() {

        /**
         * ATENÇÃO: As duas linhas a seguir são responsáveis por iniciar a navegação. A primeira linha é responsável pelo funcionamento da aplicaçao em produção,
         * A sgunda linha é repsonsável pela simulação, e deve ser utilizada somente em desenvovimento
         */
        NavigationManager.Error navigationError = AppManager.navigationManager.startNavigation(AppManager.routeGeometry.getRoute());
        //NavigationManager.Error navigationError = AppManager.navigationManager.simulate(AppManager.routeGeometry.getRoute(), 20);

        // Adiciona alguns listeners para captura de eventos como novas instruções, alerta de velocidade (VELOCIDADE AMARELA)
        AppManager.navigationManager.addNavigationManagerEventListener(new WeakReference<NavigationManager.NavigationManagerEventListener>(navigationListener));
        AppManager.navigationManager.addNewInstructionEventListener(new WeakReference<NavigationManager.NewInstructionEventListener>(newInstrictionListener));
        AppManager.navigationManager.addSpeedWarningListener(new WeakReference<NavigationManager.SpeedWarningListener>(speedWarningListener));
        AppManager.navigationManager.addPositionListener(new WeakReference<NavigationManager.PositionListener>(positionListener));

        // Valida se ao inicializar a navegação ocorreu alogum erro.
        if (navigationError == NavigationManager.Error.NONE) {


            // Muda o etado da aplicação para "Navegando", pmde alguns botões são desabilitados
            AppManager.setState(AppManager.STATE.Navigating); // state

            // Altera o tilt da aplicaçao, para melhorar a visualização da navegação (tilt = inclinação da camera)
            AppManager.map.setTilt((AppManager.map.getMaxTilt() / 4) * 3, Map.Animation.LINEAR);

            // Altera o zoom da aplicação para o maior zoom possível
            AppManager.map.setZoomLevel(AppManager.map.getMaxZoomLevel(), Map.Animation.LINEAR);

            // Altera a imagem do marker de posicionamento para uma seta, ao invés de um alvo
            try {
                Image image = new Image();
                image.setImageResource(R.drawable.nav64);
                AppManager.map.getPositionIndicator().setMarker(image);

            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    /**
     * Atualza os dados dos fragmentos de estatísticas e manobras as informações neles disponíveis, como íone, rua, próxima rua, distância para próxima rua, etc
     */
    private void updateNextManeuver(){

        // Pega a próxima manobra
        Maneuver maneuver = AppManager.navigationManager.getNextManeuver();

        // Calcula a distância. Caso a distância seja maior que 1000 metros, então é exibida em quilômetros, caso contrário, em metros sem caasa decimal
        double distance = AppManager.navigationManager.getNextManeuverDistance();
        String distanceLabel = distance >= 1000 ? Utils.metersToQuilometersLabel(distance, 1) + " km" : String.valueOf(Math.round(distance)) + " m";

        // Informa a rua atual, a próxima rua, e a distãncia para a próxima rua com a variável calculada acima
        navigationCurrRoad.setText(maneuver.getRoadName());
        navigationNextRoad.setText(String.valueOf(maneuver.getNextRoadName()));
        navigationNextDistance.setText(distanceLabel);

        // Pega o ícone responsável pela manobra e atualiza o image view
        int image = Utils.getImgFromIcon(maneuver.getIcon());
        Drawable drawable = AppManager.app.getResources().getDrawable(image);
        navigationNextImg.setImageDrawable(drawable);
    }

    /**
     * Listener responsável por capturar eventos da navegação, no nosso caso estamos utilizando para capturar o término de uma navegação.
     * O término de uma navegação se dá quando é alcançado o fim de uma rota, ou quando uma navegação é cancelada
     */
    private NavigationManager.NavigationManagerEventListener navigationListener = new NavigationManager.NavigationManagerEventListener() {

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {

            // Remove a geometria da rota do mapa e seta o tilt inicial (tilt = inclinação da camera)
            AppManager.map.removeMapObject(AppManager.routeGeometry);
            AppManager.map.setTilt(AppManager.map.getMinTilt(), Map.Animation.LINEAR);

            AppManager.navigation.hideManeuver();
            AppManager.navigation.hideStats();

            Image image = new Image();

            try {
                image.setImageResource(R.drawable.gps64);
                AppManager.setState(AppManager.STATE.Waiting); // state
                AppManager.map.getPositionIndicator().setMarker(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Listener responsável por informar quando uma nova instrução de manobra é recebida
     */
    private NavigationManager.NewInstructionEventListener newInstrictionListener = new NavigationManager.NewInstructionEventListener() {

        @Override
        public void onNewInstructionEvent() {
            // Pega a pŕoxima manora e valida se ela realmente existe
            Maneuver maneuver = AppManager.navigationManager.getNextManeuver();

            if (maneuver != null) {

                try {
                    // Atualiza os dados de estatísticas e manobras
                    updateNextManeuver();
                }
                catch(Exception e){}

                // Caso a próxima manobra seja a ultima da navegação, a próxima rua fica como "destino"
                if (maneuver.getAction() == Maneuver.Action.END)
                    navigationNextRoad.setText("Destino");
            }
        }
    };

    /**
     * Listener responsável por monitorar o posicionamento da navegação
     */
    private NavigationManager.PositionListener positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition loc) {

            // Caso a navegaçao esteja em execução
            if (AppManager.navigationManager.getRunningState() == NavigationManager.NavigationState.RUNNING) {

                try {

                    if (loc.isValid()) {

                        // Atualiza a atual velocidade do veículo
                        //todo: remover a multiplicaçao por 4
                        navigationSpeed.setText(String.valueOf(Math.round(loc.getSpeed() * 4)));

                        // Atualiza os dados de estatísticas e manobras
                        updateNextManeuver();

                        // Atualiza o tempo restante para o fim do percurso e para  apróxima manobra
                        RouteTta routeTta = AppManager.navigationManager.getTta(Route.TrafficPenaltyMode.DISABLED, true);
                        RouteTta legTta = AppManager.navigationManager.getTta(Route.TrafficPenaltyMode.DISABLED, false);

                        navigationArrival.setText(Utils.secondsPlusNowLabel(routeTta.getDuration()));
                        navigationMinsTo.setText(Utils.secondsToMinLabel(legTta.getDuration()));
                    }

                } catch(Exception e ){}
            }
        }
    };

    /**
     * Listener responsável por informar se foi ultrapassado ou se está dentro do limite de velocidade
     */
    private NavigationManager.SpeedWarningListener speedWarningListener = new NavigationManager.SpeedWarningListener() {
        @Override
        public void onSpeedExceeded(String s, float v) {
            // Caso o limite tenha sido excedido, é pintado de amarelo
            super.onSpeedExceeded(s, v);
            navigationSpeed.setTextColor(AppManager.app.getResources().getColor(R.color.warningColor));
        }
        @Override
        public void onSpeedExceededEnd(String s, float v) {
            // Em velocidade normal, a cor é padrão
            super.onSpeedExceededEnd(s, v);
            navigationSpeed.setTextColor(AppManager.app.getResources().getColor(R.color.colorAccent));
        }
    };

}
