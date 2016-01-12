package com.petro.navigator.misc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;

import com.here.android.mpa.routing.Maneuver;
import com.petro.navigator.AddPoiActivity;
import com.petro.navigator.AppManager;
import com.petro.navigator.MainActivity;
import com.petro.navigator.R;
import com.petro.navigator.SearchActivity;
import com.petro.navigator.SplashActivity;
import com.petro.navigator.SyncActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 *
 * Classe de utilidedes diversas da aplicação
 */
public class Utils {

    /**
     * Alerts de erro, alerta ou sucesso
     * @param context Contexto ao qual o alert vai se aplicar
     * @param message Mensgem
     */
    public static void showError(Context context, String message){
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Ops!")
                .setContentText(message)
                .setConfirmText("Entendi")
                .show();
    }
    public static void showAlert(Context context, String message){
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atenção!")
                .setContentText(message)
                .setConfirmText("Entendi")
                .show();
    }

    public static void showSucces(Context context, String message){
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Parabéns!")
                .setContentText(message)
                .setConfirmText("Ok")
                .show();
    }

    public static void showSuccesAddPoint(final Context context, String message){
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Parabéns!")
                .setContentText(message)
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Intent mainActivity = new Intent(context, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(mainActivity);
                    }
                })
                .show();
    }

    public static void showExit(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Tem certeza que deseja sair?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        } catch (Exception ex) {
                            android.os.Process.killProcess(id);
                        }

                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showQuestion(final Context context, String message, String title){
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setCancelText("Não")
                .setConfirmText("Sim")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        Intent addPoiActivityIntent = new Intent(context, AddPoiActivity.class);
                        context.startActivity(addPoiActivityIntent);
                    }
                })
                .show();
    }

    /**
     * Valida se existe uma conexão com a internet ativa
     * @return Se existe true, senão false
     */
    public static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) AppManager.app.getSystemService(AppManager.app.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * Pega em uma list ade manobras o nome da manobra mais longa e retorna
     * @param maneuvers
     * @return
     */
    public static String roadNameLabel(List<Maneuver> maneuvers){
        Maneuver selManeuver = null;
        for (Maneuver maneuver : maneuvers)
            if (selManeuver == null)
                selManeuver = maneuver;
            else {
                if (maneuver.getDistanceToNextManeuver() > selManeuver.getDistanceToNextManeuver())
                    if (!maneuver.getRoadName().trim().equals(""))
                        selManeuver = maneuver;
            }

        return selManeuver.getRoadName();
    }

    /**
     * Tansforma segundos em data HH:mm
     * @param seconds Segundos
     * @return hora e segundos formatados
     */
    public static String secondsToDateLabel(int seconds){

        /*int mili = seconds * 1000;
        Date date = new Date(mili);
        SimpleDateFormat formatter = new SimpleDateFormat(seconds >= 3600 ? "HH 'h' mm 'min'" : "mm 'min'", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        return formatter.format(date);*/

        try {
            int segundos = seconds;
            int segundo = segundos % 60;
            int minutos = segundos / 60;
            int minuto = minutos % 60;
            int hora = minutos / 60;

            if (hora > 24) {
                long dias = (long)Math.floor(hora / 24);
                int rest = hora % 24;
                return String.format("%02dd %02dh %02dm", dias, rest, minuto);
            }

            return String.format("%02dh %02dm", hora, minuto);
        }catch (Exception error){
            String teste = error.getMessage();
            return "";
        }
    }

    /**
     * Transforma segundos em minutos
     * @param seconds Segundos
     * @return Minutos formatados
     */
    public static String secondsToMinLabel(int seconds){

        int mili = seconds * 1000;
        Date date = new Date(mili);
        SimpleDateFormat formatter = new SimpleDateFormat("mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        return formatter.format(date);
    }

    /**
     * Adiciona segundos a hora atual e retorna formatado
     * @param seconds Segundos
     * @return Data hora atual + segundos informados formatados
     */
    public static String secondsPlusNowLabel(int seconds){

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        Date now = new Date();
        long curr = now.getTime();
        //long curr = TimeZone.getDefault().getOffset(now.getTime());
        //curr = curr - ((3600) * 1000);
        //curr = curr - ((3600) * 1000);
        long mili = seconds * 1000;
        Date date = new Date(curr + mili);

        return formatter.format(date);
    }

    /**
     * Transforma metros para quilometros
     * @param meters Metros
     * @param round Casas para arredondamento
     * @return Quilômetros
     */
    public static String metersToQuilometersLabel(double meters, int round)
    {
        double kilometers = meters / 1000;
        int temp = (int)(kilometers * Math.pow(10 , round));
        double number = ((double)temp)/Math.pow(10 , round);

        return Double.toString(number);
    }

    /**
     * Transforma kbytes para mbytes
     * @param kilo Kilobytes
     * @param round Casas para arredondamento
     * @return Megabytes
     */
    public static String kiloToMegaLabel(double kilo, int round)
    {
        double mega = kilo / 1024;
        int temp = (int)(mega * Math.pow(10 , round));
        double number = ((double)temp)/Math.pow(10 , round);

        return Double.toString(number);
    }

    /**
     * Retorna a String do InputStream
     * @param ists InputStream
     * @return String do InputStream
     */
    public static String convertinputStreamToString(InputStream ists)
            throws IOException {
        if (ists != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader r1 = new BufferedReader(new InputStreamReader(
                        ists, "UTF-8"));
                while ((line = r1.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                ists.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Retorna o identificador de uma imagem de acordo com o tipo
     * @param icon Tipo do ícone
     * @return Identificador da imagem selecionada
     */
    public static int getImgFromIcon(Maneuver.Icon icon){

        switch (icon) {

            case UNDEFINED:
                return -1;

            case START:
            case GO_STRAIGHT:
            case FERRY:
            case PASS_STATION:
            case HEAD_TO:
            case CHANGE_LINE:
                return R.drawable.straigt_64;

            case KEEP_MIDDLE:
                return R.drawable.keep_middle_64;

            case KEEP_LEFT:
            case HIGHWAY_KEEP_LEFT:
                return R.drawable.keep_left_64;

            case KEEP_RIGHT:
            case HIGHWAY_KEEP_RIGHT:
                return R.drawable.keep_right_64;

            case UTURN_RIGHT:
                return R.drawable.uturn_right_64;

            case UTURN_LEFT:
                return R.drawable.uturn_left_64;

            case LIGHT_RIGHT:
                return R.drawable.light_right_64;

            case QUITE_RIGHT:
                return R.drawable.right_64;

            case HEAVY_RIGHT:
                return R.drawable.heavy_right_64;

            case LIGHT_LEFT:
                return R.drawable.light_left_64;

            case QUITE_LEFT:
                return R.drawable.left_64;

            case HEAVY_LEFT:
                return R.drawable.heavy_left_64;

            case ENTER_HIGHWAY_RIGHT_LANE:
                return R.drawable.entrance_left_64;

            case ENTER_HIGHWAY_LEFT_LANE:
                return R.drawable.entrance_right_64;

            case LEAVE_HIGHWAY_RIGHT_LANE:
                return R.drawable.exit_right_64;

            case LEAVE_HIGHWAY_LEFT_LANE:
                return R.drawable.exit_left_64;

            case ROUNDABOUT_1_LH:
            case ROUNDABOUT_1:
                return R.drawable.roundabout_1_64;

            case ROUNDABOUT_2_LH:
            case ROUNDABOUT_2:
                return R.drawable.roundabout_2_64;

            case ROUNDABOUT_3_LH:
            case ROUNDABOUT_3:
                return R.drawable.roundabout_3_64;

            case ROUNDABOUT_4:
            case ROUNDABOUT_5:
            case ROUNDABOUT_6:
            case ROUNDABOUT_7:
            case ROUNDABOUT_8:
            case ROUNDABOUT_9:
            case ROUNDABOUT_10:
            case ROUNDABOUT_11:
            case ROUNDABOUT_12:
            case ROUNDABOUT_4_LH:
            case ROUNDABOUT_5_LH:
            case ROUNDABOUT_6_LH:
            case ROUNDABOUT_7_LH:
            case ROUNDABOUT_8_LH:
            case ROUNDABOUT_9_LH:
            case ROUNDABOUT_10_LH:
            case ROUNDABOUT_11_LH:
            case ROUNDABOUT_12_LH:
                return R.drawable.roundabout_64;

            case END:
                return R.drawable.pin64;

            default:
                return -1;

        }
    }
}
