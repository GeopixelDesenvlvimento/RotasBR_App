package com.petro.navigator.dao;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Classe de gestão de dados da Localidade (POI)
 */
public class LocationDAO extends BaseDAO {

    /**
     * Construtor
     * @param context Contexto para instância do banco de dados
     */
    public LocationDAO(Context context) {
        super(context);

        TABLENAME = "LOCALIDADE";
        ORDERBY = "DESCRICAO COLLATE NOCASE";
    }

    /**
     * Insere registros
     * @param title
     * @param description
     * @param lon
     * @param lat
     * @return Se a inserção foi realizada com sucesso
     */
    public boolean insert(String title, String description, float lon, float lat) {

        // Cria uma lista de campos e adiciona os respectivos identificadores e valores
        List<Field> fields = new ArrayList<Field>();

        fields.add(new Field("TITULO", title));
        fields.add(new Field("DESCRICAO", description));
        fields.add(new Field("LON", lon));
        fields.add(new Field("LAT", lat));

        return super.insert(fields);
    }

    /**
     * Retorna todos os registros
     * @return Cursor com todos os dados
     */
    public Cursor all() {

        // Cria uma lista de campos e adiciona os respectivos identificadores
        List<Field> fields = new ArrayList<Field>();

        fields.add(new Field("ID"));
        fields.add(new Field("TITULO"));
        fields.add(new Field("DESCRICAO"));
        fields.add(new Field("LON"));
        fields.add(new Field("LAT"));

        return super.all(fields);
    }

    /**
     * Realiza o like na tabela
     * @param value Valor a ser pesquisado
     * @return Cursor como resultado
     */
    public Cursor like(String value){

        // Cria uma lista de campos e adiciona os respectivos identificadores
        List<Field> fields = new ArrayList<Field>();

        fields.add(new Field("ID"));
        fields.add(new Field("TITULO"));
        fields.add(new Field("DESCRICAO"));
        fields.add(new Field("LON"));
        fields.add(new Field("LAT"));

        // Cria o campo com o identificador e valor de onde o like será realizado
        Field like = new Field("DESCRICAO", value);

        return super.like(fields, like);
    }
}
