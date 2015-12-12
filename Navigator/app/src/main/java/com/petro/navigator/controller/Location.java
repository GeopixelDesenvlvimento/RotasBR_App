package com.petro.navigator.controller;

import android.content.Context;
import android.database.Cursor;

import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.petro.navigator.AppManager;

import com.petro.navigator.dao.LocationDAO;
import com.petro.navigator.misc.Utils;
import com.petro.navigator.model.LocationModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Classe de gestão das localidades (POIs)
 */
public class Location implements Serializable {

    // Variáveis
    private LocationDAO dao; // DATA ACCESS OBJECT da classe Location
    private List<LocationModel> locations = new ArrayList<LocationModel>(); // Lista das localizações atuais, retornada pelo banco de dados

    /**
     * Gesture Listener: responsável por capturar eventos relacionados a gestos no mapa, como a seleção de um MapMarker, por exemplo
     */
    private MapGesture.OnGestureListener gestureListener = new MapGesture.OnGestureListener.OnGestureListenerAdapter() {
        @Override
        public boolean onMapObjectsSelected(List<ViewObject> objects) {
            for (ViewObject viewObj : objects) {

                if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {

                    // Valida se é um marker, como só existem marker de localidade no mapa, não é necessário validar alguma informaçõa adicional
                    if (((MapObject)viewObj).getType() == MapObject.Type.MARKER) {

                        // Utiliza o método this.set para armazenar o objecto selecionado em uma variável global
                        Location.this.set((MapMarker) viewObj);
                    }
                }
            }
            // return false to allow the map to handle this callback also
            return false;
        }
    };

    /**
     * Cpsmtritpr
     * @param context Contexto para inicialização do DAO
     * @param mockData Mocka dados para teste. Caso fique sempre como true, toda vez que a aplicação for iniciada, serão isneridos novos dados no banco de dados.
     */
    public Location(Context context, boolean mockData) {

        // Adiciona o Gesture Listener ao map fragmento
        AppManager.mapFragment.getMapGesture().addOnGestureListener(gestureListener);

        // Inicializa o DAO
        dao = new LocationDAO(context);

        // Mocka os dados
        //if (mockData) mockData();

        // Busca, preenche a lista  e plota no mapa os dados.
        //fill();
    }

    /**
     * Pupula o mapa com os objetos recuperados do banco de dados;
     */
    private void fill(){

        // Busca e serializa os dados
        this.all();

        // Adiciona os dados recuperados no mapa
        for(LocationModel location : locations) {
            AppManager.map.addMapObject(location.getMarker());
            location.getMarker().showInfoBubble();
        }
    }

    /**
     * Preenche a variável global selectedLocation com o marker selecionado no mapa ou na lista de pesquisa
     * @param marker o marker que foi selecionado no mapa ou na lista de pesquisa
     */
    public void set(MapMarker marker){

        // Adiciona o marker à variavel global
        AppManager.selectedLocation = marker;
        AppManager.selectedLocation.showInfoBubble();

        // Valida se o GPS já capturou alguma localização, e caso tenhas capturado, realiza o cálculo da rota, caso contrário, exibe uma mensagem para aguardar o GDP
        if (AppManager.currentPosition != null)
            AppManager.routing.calc();
        else
            Utils.showAlert(AppManager.app, "Aguarde o sinal do GPS!");
    }
    public void set (LocationModel location) { set(location.getMarker()); }

    /**
     * Serializa o cursor com itens que foi retornado pelo DAO
     * @param cursor cursor de itens
     * @return lista de LocationModel, modelo referente à localização
     */
    private List<LocationModel> serialize(Cursor cursor) {

        // Zera a lista de localização
        locations = new ArrayList<LocationModel>();

        // Valida se o cursor contém algum dado
        if (cursor.getCount() > 0) {

            // Para cada registro do cursor, armazena um modelo (objeto) na lista de localidades
            do {
                locations.add(new LocationModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8)));
            }
            while (cursor.moveToNext());
        }

        return locations;
    }

    /**
     * Busca todos os registros do banco de dados.
     * @return Lista de modelo de localidade
     */
    public List<LocationModel> all(){

        // Serializa os registros que foram recuperados do banco de dados
        return serialize( dao.all() );
    }

    /**
     * Busca os registros do banco de dados via like no campo descrição
     * @param value valor par ao like
     * @return Lista de modelo de localidade
     */
    public List<LocationModel> like(String value){

        return serialize( dao.like(value) );
    }

    public List<LocationModel> likeMoreFields(String stateValue, String typeValue, String contentValue , String idValue){

        return serialize( dao.likeMoreFields(stateValue, typeValue, contentValue, idValue) );
    }

    public Integer likeMoreFieldsCount(String stateValue, String typeValue, String contentValue , String idValue){

        return Integer.parseInt(dao.likeMoreFieldsCount(stateValue, typeValue, contentValue, idValue).getString(0).toString());
    }


    /**
     * Busca os registros do banco de dados via like no campo descrição
     * @return Lista de modelo de localidade
     */
    public List<LocationModel> lastValue(){

        return serialize(dao.lastValue());
    }

    /**
     * Busca os registros do banco de dados via like no campo descrição
     * @return Lista de modelo de localidade
     */
    public List<LocationModel> getValeuGroupBy(String field){

        return serialize( dao.getValeuGroupBy(field) );
    }

    /**
     * Busca os registros do banco de dados via like no campo descrição
     * @return Lista de modelo de localidade
     */
    public List<LocationModel> WhereClause(String whereClause, String field){

        return serialize( dao.WhereClause(whereClause, field) );
    }

    /**
     * Busca todos os registros do banco de dados.
     * @return Lista de modelo de localidade
     */
    public void deleteAll(String whereClause){

        // Serializa os registros que foram recuperados do banco de dados
         dao.deleteAll(whereClause);
    }

    /**
     * Pega localidade da lista baseada na descrição
     * @param desc Descriçaõ para comparação
     * @return Modelo de localidade
     */
    public LocationModel get(String desc){

        // Para cada modelo de localidade na lista privada, valida as descriçãoes e retorna a com descrição igual
        // Caso não encontre um descrição correspondente, retorna nulo
        for (LocationModel location : locations)
            if (location.getDesc().equals(desc))
                return location;
        return null;
    }

    /**
     * Zoom para a localização
     * @param location Localização na qua será aplicada o zoom
     */
    public void zoomTo(LocationModel location){
        AppManager.map.setCenter(location.getMarker().getCoordinate(), Map.Animation.LINEAR);
    }

    /**
     * Adicionar o ponto no mapa
     * @param location Localização que será adicionada no mapa
     */
    public void addMarkerMap(LocationModel location){
        AppManager.map.addMapObject(location.getMarker());
        location.getMarker().showInfoBubble();
    }

    /**
     * Método que mpcka dados no banco de dados. Deve ser retirado na versão final da app
     */
    public void mockData() {

        // Insere os pontos em um veto TODO: COMENTADO POR CLAYTON, VERIFICAR SE PRECISA RETIRAR O COMENTÁRIO OU REMOVER DO CÓDIGO
        //List<LocationModel> points = new ArrayList<LocationModel>();
        //points.add(new LocationModel("BA","Poço","BP","Localidade", "Restaurante Mexicano",        (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Comemoração", (float) -45.8871903839996   , (float) -23.179239600999626 ));
        //points.add(new LocationModel("BA","Poço","BP","Localidade", "teste",                                      (float) -45.891551633505351 , (float) -23.169872112666045 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Atuação",    (float) -45.900516162999622 , (float) -23.157171200999588 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Ponto de encontro",    (float) -45.893566406999639 , (float) -23.249376388999639 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Ponto de encontro 2",    (float) -45.86094854099963  , (float) -23.205829497999613 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Nasci aqui",    (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Ponte de referência 2",                                 (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Local de onde eu vim",    (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("SP","Poço","BP","Localidade", "Onde vim parar",    (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("BA","Poço","BP","Localidade", "Local importante",    (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("BA","Poço","BP","Localidade", "Ponto de Referência",    (float) -45.886938233999615 , (float) -23.179439339999607 ));
        //points.add(new LocationModel("BA","Poço","BP","Localidade", "Local importante",    (float) -45.879505966999595 , (float) -23.193259803999588 ));
        //points.add(new LocationModel("Localidade", "Sou natural de Belo Horizonte e me mude",    (float) -45.881199586999628 , (float) -23.195392134999615 ));

        // Para cada ponto, insere no mapa e no banco
        /*for (LocationModel point : points) {

            AppManager.map.addMapObject(point.getMarker());
            dao.insert(point.getUf(), point.getType(), point.getContext(), point.getTitle(), point.getDesc(),point.getLon(), point.getLat() );
        }*/
    }

    public void InserDB(List<LocationModel> points) {

        // Insere os pontos em um veto TODO: COMENTADO POR CLAYTON, VERIFICAR SE PRECISA RETIRAR O COMENTÁRIO OU REMOVER DO CÓDIGO
        // Para cada ponto, insere no mapa e no banco
        for (LocationModel point : points) {

            AppManager.map.addMapObject(point.getMarker());
            dao.insert(point.getUf(), point.getType(), point.getContext(), point.getTitle(), point.getDesc(), point.getDate(), point.getLon(), point.getLat() );
        }
    }
}

