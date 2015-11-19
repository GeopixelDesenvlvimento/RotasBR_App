package com.petro.navigator.misc;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.here.android.mpa.odml.MapPackage;
import com.petro.navigator.AppManager;
import com.petro.navigator.R;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Classe auxiliar renderer da lista de pacotes disponíveis
 */
public class MapPackageAdapter extends ArrayAdapter<String> {

    // Variáveis
    // Contexto
    private final Activity context;

    /**
     * Construtor
     * @param context Contexto da app
     * @param size Array somente para a quantidade, não necessita ter valores
     */
    public MapPackageAdapter(Activity context, String[] size) {
        super(context, R.layout.map_package_item, size);
        this.context = context;
    }

    /**
     * Método sobreescrito para renderização dos valores de cada linha do listview
     * @param position Posição do item que está sendo renderizado
     * @param view Atual View
     * @param parent Parent da atual view
     * @return Retorna a view completa e customizada
     */
    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.map_package_item, null, true);

        // Pèga do lista global de pacotes de mapa o pacore correspondente à posição do item
        MapPackage mapPackage = AppManager.mapPackages.get(position);

        // Inicializa variáveis da atual view  e o título e o tamanho do pacote
        final ImageView packageImg = (ImageView)rowView.findViewById(R.id.map_package_img);
        final TextView packageTitle = (TextView) rowView.findViewById(R.id.map_package_title);
        final TextView packageStatus = (TextView) rowView.findViewById(R.id.map_package_status);
        final TextView packageSize = (TextView) rowView.findViewById(R.id.map_package_size);
        final ProgressBar packagePercentage = (ProgressBar) rowView.findViewById(R.id.map_package_percentage);

        packageTitle.setText(mapPackage.getTitle());
        packageSize.setText(Utils.kiloToMegaLabel(mapPackage.getSize(), 1) + " mb");

        // Estado da instalação
        MapPackage.InstallationState state = mapPackage.getInstallationState();

        if (AppManager.getState() == AppManager.STATE.Syncing && mapPackage.getId() == AppManager.mapPackegeId) {
            packageImg.setImageResource(R.drawable.hourglass_64);
            packageStatus.setText(R.string.map_package_installing);
            packagePercentage.setProgress(AppManager.mapPackagePercentage.getProgress());
        }
        else {
            // Valida se o pacote está instalado e seta os comportamentos
            if (state == MapPackage.InstallationState.INSTALLED) {
                packageImg.setImageResource(R.drawable.trash_64);
                packageStatus.setText(R.string.map_package_installed);
                packagePercentage.setProgress(100);
            } else {
                packageImg.setImageResource(R.drawable.download_64);
                packageStatus.setText(R.string.map_package_not_installed);
                packagePercentage.setProgress(0);
            }
        }

        // Adiciona um listener para click no botão
        packageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Caso exista algum sincronismo em andamento, neminha ação é executada e uma mensagem é exibgida
                if (AppManager.getState() == AppManager.STATE.Syncing) {
                    Utils.showAlert(context, "Existe um sincronismo em andamento!");
                    return;
                }

                // Adiciona par avariáveis globais os controles
                AppManager.mapPackageImg = packageImg;
                AppManager.mapPackageTitle = packageTitle;
                AppManager.mapPackageStatus = packageStatus;
                AppManager.mapPackageSize = packageSize;
                AppManager.mapPackagePercentage = packagePercentage;

                // Pega o pacote, atualiza o etado da aplicação para "sincronizando"
                MapPackage mapPackage = AppManager.mapPackages.get(position);
                MapPackage.InstallationState state = mapPackage.getInstallationState();

                AppManager.setState(AppManager.STATE.Syncing);

                // Cria uma lista com os ids para instalação
                List<Integer> mapPackageIds = new ArrayList<Integer>();
                mapPackageIds.add(mapPackage.getId());

                boolean done;

                AppManager.mapPackegeId = mapPackage.getId();

                // Dependendo do estado do pacote, instala ou  desinstala o pacote
                if (state == MapPackage.InstallationState.INSTALLED)
                    done = AppManager.mapLoader.uninstallMapPackages(mapPackageIds);
                else
                    done = AppManager.mapLoader.installMapPackages(mapPackageIds);

                // Caso existem problemas durante a instalação ou desisntalação do pacote, uma mensagem é exibida
                if (!done)
                    Utils.showError(context, "Problemas ao" + (state == MapPackage.InstallationState.INSTALLED ? " remover " : " instalar ") + "o pacote " + mapPackage.getTitle());
            }
        });

        // retorna a view
        return rowView;
    }
}