package com.petro.navigator.controller;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.mapping.Map;
import com.petro.navigator.AppManager;

import java.lang.ref.WeakReference;

/**
 *
 * Classe de gerenciamento da posição (GPS) do dispositivo
 */
public class Position {

    /**
     * Listener responsável por monitorar a localização do GPS
     */
    private PositioningManager.OnPositionChangedListener positionListener = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {

            // Verifica se a posiçaõ obtida é válida
            if (geoPosition.isValid()){

                // Atualiza a posião atual
                AppManager.currentPosition = geoPosition.getCoordinate();

                if (!AppManager.currentPositionZoomTo) {

                    AppManager.map.setCenter(geoPosition.getCoordinate(), Map.Animation.LINEAR, (AppManager.map.getMaxZoomLevel() / 3) * 2, (float) geoPosition.getHeading(), AppManager.map.getMinTilt());
                    AppManager.currentPositionZoomTo = true;
                }
            }
        }

        /**
         * Override necessário para a implementação
         * @param locationMethod
         * @param locationStatus
         */
        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

            //determine if tunnel extrapolation is active
            if (locationMethod == PositioningManager.LocationMethod.GPS){
                boolean isExtrapolated = ((AppManager.positioningManager.getRoadElement() != null) && (AppManager.positioningManager.getRoadElement().getAttributes().contains(RoadElement.Attribute.TUNNEL)));
                boolean hasGPS = (locationStatus == locationStatus.AVAILABLE);
            }
        }
    };

    /**
     * Construtor
     */
    public Position(){
        // Cria uma nova instância do positioning manager, informa o listener responsável por manter atualizada a psição e inicia o monitoramento.
        AppManager.map.setCenter(new GeoCoordinate(-12.9968881,
                -38.4671059), Map.Animation.NONE);//Centraliza o mapa inicial em Salvado, onde está a Sede da Empresa.
        AppManager.positioningManager = PositioningManager.getInstance();
        AppManager.positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(positionListener));
        AppManager.positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);

        // Seta o indicador da posição padrão do SDK como visível
        AppManager.map.getPositionIndicator().setVisible(true);
    }

    /**
     * Zoom para a ultima posição conhecida do GPS
     */
    public void zoomToCurrentPosition(){

        AppManager.map.setCenter(AppManager.currentPosition, Map.Animation.LINEAR);
    }
}
