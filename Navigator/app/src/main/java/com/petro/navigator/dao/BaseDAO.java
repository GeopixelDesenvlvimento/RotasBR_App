package com.petro.navigator.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.petro.navigator.db.DataBase;

import java.util.List;

/**
 *
 * Classe de gestão de dados
 */
public abstract class BaseDAO {

    public String TABLENAME = "";
    public String ORDERBY = "";

    /**
     * Classe auxiliar field
     */
    public class Field {
        public Field(String _name){
            name = _name;
        }
        public Field(String _name, String _value ){
            name = _name;
            value =_value;
        }
        public Field(String _name, float _value ){
            name = _name;
            value =_value;
        }
        public Field(String _name, int _value ){
            name = _name;
            value =_value;
        }
        public Field(String _name, boolean _value ){
            name = _name;
            value =_value;
        }

        String name = "";
        Object value;
    }
    private SQLiteDatabase db;
    private DataBase dbManager;

    public BaseDAO(Context context) {
        dbManager = new DataBase(context);
    }

    /**
     * Pega o tipo do valor
     * @param value O valor
     * @return O tipo
     */
    private String getType(Object value){

        try{
            Integer.parseInt(value.toString());
            return "int";
        }catch(Exception e){ }
        try{
            Float.parseFloat(value.toString());
            return "float";
        }catch(Exception e){ }
        return "string";
    }

    /**
     * Insere registros
     * @param fields Lista de campos com valores
     * @return Se a inserção foi realizada com sucesso
     */
    public boolean insert(List<Field> fields) {

        db = dbManager.getWritableDatabase();

        ContentValues values = new ContentValues();

        for(Field field : fields) {

            Log.e("FIELD VALUE", field.value.toString());

            switch (getType(field.value)){
                case "int":
                    values.put(field.name, Integer.parseInt(field.value.toString()));
                    break;
                case "float":
                    values.put(field.name, Float.parseFloat(field.value.toString()));
                    break;
                default:
                    values.put(field.name, field.value.toString());
                    break;
            }
        }

        long result = db.insert(TABLENAME, null, values);

        db.close();

        if (result == -1)
            System.out.println("**" + TABLENAME + "** >> ERRO ao inserir os dados");
        else
            System.out.println("**" + TABLENAME + "** >> SUCESSO ao inserir os dados");

        return !(result == -1);
    }

    /**
     * Retorna todos os registros
     * @param fields Lista de campos
     * @return Cursor com todos os dados
     */
    public Cursor all(List<Field> fields) {

        return like(fields, null);
    }

    /**
     * Realiza o like na tabela
     * @param fields Lista de campos
     * @param like Campo para o like com valor
     * @return Cursor como resultado
     */
    public Cursor like(List<Field> fields, Field like){

        db = dbManager.getReadableDatabase();
        String fieldStr = "";

        for(Field field : fields)
            fieldStr += (fieldStr.equals("") ? "" : ",") + field.name;

        String select = "SELECT " + fieldStr+ " FROM " + TABLENAME +

                (like == null || like.value.toString().equals("") ? "" : " WHERE " + like.name + " LIKE '%" + like.value.toString() + "%' " ) +
                (ORDERBY.equals("") ? "" : " ORDER BY "+ ORDERBY );

        Log.d("SELECT" , select);

        Cursor cursor = db.rawQuery(select, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();

        return cursor;
    }
}
