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

    public Cursor likeMoreFields(List<Field> fields, String ValueFieldS, String ValueFieldT, String ValueFieldC, String ValueFieldID){

        db = dbManager.getReadableDatabase();
        String fieldStr = "";

        for(Field field : fields)
            fieldStr += (fieldStr.equals("") ? "" : ",") + field.name;

        String select = "SELECT " + fieldStr+ " FROM " + TABLENAME + " WHERE ";
                if(!ValueFieldS.isEmpty() && !ValueFieldS.equals("Selecione Estado"))
                    select+= " UF = '" + ValueFieldS.toString() + "' and ";
                if(!ValueFieldT.isEmpty() && !ValueFieldT.equals("Selecione Classe"))
                    select+= " TYPE = '" + ValueFieldT.toString() + "' and ";
                if(!ValueFieldC.isEmpty() && !ValueFieldC.equals("Selecione Contexto"))
                    select+= " CONTEXT = '" + ValueFieldC.toString() + "' and ";
                if(!ValueFieldID.isEmpty())
                    select+= " DESCRICAO LIKE '%" + ValueFieldID.toString() + "%' and ";

        select+= " 1=1";
        Log.d("SELECT" , select);

        Cursor cursor = db.rawQuery(select, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();

        return cursor;
    }

    public Cursor likeMoreFieldsCount(String ValueFieldS, String ValueFieldT, String ValueFieldC, String ValueFieldID){

        db = dbManager.getReadableDatabase();
        String fieldStr = "";

        String select = "SELECT COUNT(ID) FROM " + TABLENAME + " WHERE ";
        if(!ValueFieldS.isEmpty() && !ValueFieldS.equals("Selecione Estado"))
            select+= " UF = '" + ValueFieldS.toString() + "' and ";
        if(!ValueFieldT.isEmpty() && !ValueFieldT.equals("Selecione Classe"))
            select+= " TYPE = '" + ValueFieldT.toString() + "' and ";
        if(!ValueFieldC.isEmpty() && !ValueFieldC.equals("Selecione Contexto"))
            select+= " CONTEXT = '" + ValueFieldC.toString() + "' and ";
        if(!ValueFieldID.isEmpty())
            select+= " DESCRICAO LIKE '%" + ValueFieldID.toString() + "%' and ";

        select+= " 1=1";
        Log.d("SELECT" , select);

        Cursor cursor = db.rawQuery(select, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();

        return cursor;
    }


    /**
     * Retorna o último valor da Tabela POI cadastrada
     * @param equals Campo para o like com valor
     * @return Cursor como resultado
     */
    public Cursor lastValue(Field equals){

        db = dbManager.getReadableDatabase();
        String fieldStr = "";

        String select = "SELECT ID, TITULO, DESCRICAO, UF, CONTEXT, TYPE, VAL, LAT, LON FROM " + TABLENAME + " WHERE 1 = 1 ORDER BY " + equals.name + " DESC ";

        Log.d("SELECT" , select);

        Cursor cursor = db.rawQuery(select, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();

        return cursor;
    }

    /**
     * Retorna o último valor da Tabela POI cadastrada
     * @param whereClause Campo para o like com valor
     * @return Cursor como resultado
     */
    public Cursor WhereClause(String whereClause, String field){

        db = dbManager.getReadableDatabase();
        String fieldStr = "";

        String select = "SELECT ID, UF, TYPE, CONTEXT, TITULO, DESCRICAO, VAL, LAT, LON FROM " + TABLENAME + " WHERE " + whereClause + " GROUP BY " + field;

        Log.d("SELECT" , select);

        Cursor cursor = db.rawQuery(select, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();

        return cursor;
    }

    /**
     * Realiza o like na tabela
     * @param field Campo para o like com valor
     * @return Cursor como resultado
     */
    public Cursor getValeuGroupBy(String field){

        db = dbManager.getReadableDatabase();

        String select = "SELECT ID, UF, TYPE, CONTEXT, TITULO, DESCRICAO, VAL, LAT, LON FROM " + TABLENAME + " GROUP BY " + field;

        Log.d("SELECT" , select);

        Cursor cursor = db.rawQuery(select, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();

        return cursor;
    }

    /**
     * Realiza o like na tabela
     * @param whereClause Campo para o like com valor
     * @return Cursor como resultado
     */
    public void deleteAll(String whereClause){

        db = dbManager.getReadableDatabase();

        String delete = "DELETE FROM " + TABLENAME + " WHERE " + whereClause;

        Log.d("DELETE" , delete);

        Cursor cursor = db.rawQuery(delete, null);
        if (cursor != null) cursor.moveToFirst();

        db.close();
    }
}
