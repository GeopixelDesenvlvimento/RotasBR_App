package com.petro.navigator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.here.android.mpa.odml.MapLoader;
import com.here.android.mpa.odml.MapPackage;
import com.petro.navigator.misc.MapPackageAdapter;
import com.petro.navigator.misc.Utils;

import java.util.ArrayList;

/**
 *
 * Activity splash screen
 *
 */
public class SyncActivity extends AppCompatActivity {

    // Variáveis
    // Controle de liste de itens de pacotes de mapa
    private ListView syncList;

    private Activity self;

    /**
     * Construtor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Gerenciar mapas off-line");

        self = this;

        syncList =(ListView)findViewById(R.id.sync_list);

        AppManager.mapLoader.addListener(mapLoaderListener);
        AppManager.mapLoader.getMapPackages();
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
        AppManager.mapLoader.removeListener(mapLoaderListener);
    }

    /**
     * Caso uma sincronização  esteja sendo realizada, não deixa sair da tela até o término da mesma
     */
    @Override
    public void onBackPressed() {
        if (AppManager.getState() != AppManager.STATE.Syncing)
            super.onBackPressed();
        else
            Utils.showAlert(this, "Existe um sincronismo em andamento!");
    }

    /**
     * Carreag a lista no ListView
     */
    private void loadList(){

        // Limpa o adapter e atualiz a lista
        syncList.setAdapter(null);
        syncList.refreshDrawableState();

        // Pega o tamanho referente a quantidade de arquivos na lista global de pacotes de mapa
        String[] size = new String[AppManager.mapPackages.size()];

        // Cria o adaptador customizado em misc.MapPackageAdapater
        MapPackageAdapter adapter = new MapPackageAdapter(SyncActivity.this, size);

        //Informa o adapter e atualiza o view
        syncList.setAdapter(adapter);
        syncList.refreshDrawableState();
    }

    /**
     * Inicializa os pacotes de mapa. Existe uma estrutrura de árvore, onde o primeiro pacote ramifica para n pacotes
     * onde cada um desses n pacotes ramificam para mais m pacotes, ou seja, é um função com chamada recursiva
     * @param mapPackage O pacote de mapa atual
     * @param store Indica se o pacote de mapa atual deve ser armazenado noa lista gloobal
     */
    private void initMapPackages(MapPackage mapPackage, boolean store){

        // Verfiica se o pacote de mapa nao é nulo
        if (mapPackage != null) {

            // Verifica se deve ser armazenado na lista global de pacotes de maapa
            if (store)
                AppManager.mapPackages.add(mapPackage);

            // Verifica se o pacote atual é o Brasil, caso seja, todos os pactoes filhos serão armazenados na lista global de pacotes de maaps
            boolean addInList = store ?  true : (mapPackage.getTitle().toUpperCase().equals("BRAZIL") || mapPackage.getTitle().toUpperCase().equals("BRASIL") ? true : false);

            // Para cada pacote de mapa filho, a mesma função (recursiva) é executada
            for (MapPackage child : mapPackage.getChildren())
                initMapPackages(child, addInList);
        }
    }

    /**
     * Listener responsável por capturar os eventos relacionados à instação, desinstação e progresso dos pacotes de amap
     */
    MapLoader.Listener mapLoaderListener = new MapLoader.Listener() {

        /**
         * Quandoa a remoção de um pacote é concluída
         */
        public void onUninstallMapPackagesComplete(MapPackage rootMapPackage, MapLoader.ResultCode mapLoaderResultCode) {
            AppManager.mapPackageImg.setImageResource(R.drawable.download_64);
            AppManager.mapPackageStatus.setText(R.string.map_package_not_installed);
            AppManager.mapPackagePercentage.setProgress(0);

            AppManager.setState(AppManager.STATE.Sync);
            AppManager.mapLoader.getMapPackages();
        }

        /**
         * Quando a instação de um pacote está concluíoda
         */
        public void onInstallMapPackagesComplete(MapPackage rootMapPackage, MapLoader.ResultCode mapLoaderResultCode) {
            AppManager.mapPackageImg.setImageResource(R.drawable.trash_64);
            AppManager.mapPackageStatus.setText(R.string.map_package_installed);
            AppManager.mapPackagePercentage.setProgress(100);

            AppManager.setState(AppManager.STATE.Sync);
            AppManager.mapLoader.getMapPackages();
        }

        /**
         * Qaundo a solicitação dos pacotes de mapa dipníveis é concluṕida
         * @param rootMapPackage O primeiro elemento dos pacotes de mapa disponíveis
         * @param mapLoaderResultCode Resultado do carregamento
         */
        public void onGetMapPackagesComplete(MapPackage rootMapPackage, MapLoader.ResultCode mapLoaderResultCode) {

            // shows loading
            AppManager.hideView(self, R.id.sync_loading);
            AppManager.showView(self, R.id.sync_list);

            // Limpa a variaǘel global referente aps pacotes de mapa
            AppManager.mapPackages = new ArrayList<MapPackage>();

            // Inicializa os pacotes de mapa
            initMapPackages(rootMapPackage, false);

            // Carrega o list view
            loadList();
        }

        /**
         * Ao correr do processo, a porcentagem é atualizada
         * @param progressPercentage Percentual de progresso
         */
        public void onProgress(int progressPercentage) {

            AppManager.mapPackagePercentage.setProgress(progressPercentage);
        }

        /**
         * Metodos necessários para implementação, porém não utilizados
         */
        public void onPerformMapDataUpdateComplete(MapPackage rootMapPackage, MapLoader.ResultCode mapLoaderResultCode) {
        }
        public void onInstallationSize(long diskSize, long networkSize) {
        }
        public void onCheckForUpdateComplete(boolean updateAvailable, String currentMapVersion,String newestMapVersion, MapLoader.ResultCode mapLoaderResultCode) {
        }
    };
}
