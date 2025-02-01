package com.axel_stein.noteapp.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.main.MainActivity;
import com.axel_stein.noteapp.settings.SettingsContract.View;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;
import static com.axel_stein.noteapp.utils.FileUtil.writeToFile;

public class SettingsPresenter implements SettingsContract.Presenter {

    private interface BillingClientReadyCallback {
        void onBillingClientReady(BillingClient client);
    }

    @Inject
    CreateBackupInteractor mCreateBackupInteractor;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    @Inject
    AppSettingsRepository mSettings;

    private @Nullable View mView;
    private @Nullable Context mContext;
    private @Nullable BillingClient billingClient;
    private @Nullable FragmentActivity activity;
    private boolean billingConnection;

    @Override
    public void onCreate(FragmentActivity activity) {
        this.activity = activity;
        App.getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        this.activity = null;
    }

    @Override
    public void onCreateView(View view) {
        mView = view;
        mContext = mView.getContext();

        String title = mContext.getString(R.string.app_version_title);
        String number = mContext.getString(R.string.app_version_number);
        mView.setAppVersion(String.format("%s %s", title, number));
    }

    @Override
    public void onDestroyView() {
        mView = null;
        mContext = null;
    }

    @Override
    public void onPreferenceClick(String key) {
        if (mView == null) return;

        switch (key) {
            case "export_file":
                createBackup();
                break;

            case "import_file":
                mView.startImportFileActivity();
                break;

            case "rate_app":
                mView.startRateAppActivity();
                break;

            case "disable_ads":
                disableAds();
                break;

            case "restore_purchases":
                restorePurchase();
                break;
        }
    }

    private void disableAds() {
        getBillingClient(client -> {
            ArrayList<QueryProductDetailsParams.Product> products = new ArrayList<>();
            products.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("no_ads")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            );

            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(products)
                    .build(),
                (billingResult, list) -> {
                    ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParams = ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(list.get(0))
                            .build()
                    );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParams)
                        .build();

                    billingClient.launchBillingFlow(activity, billingFlowParams);
                }
            );
        });
    }

    private void restorePurchase() {
        getBillingClient(client -> client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            (billingResult, list) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (list.isEmpty()) {
                        if (mView != null) {
                            mView.showMessage(R.string.no_purchases_found);
                        }
                    } else {
                        hideAds();
                    }
                } else if (mView != null) {
                    mView.showMessage(billingResult.getDebugMessage());
                }
            }
        ));
    }

    private void getBillingClient(BillingClientReadyCallback callback) {
        if (billingClient != null) {
            callback.onBillingClientReady(billingClient);
            return;
        }
        if (billingConnection) {
            return;
        }
        billingConnection = true;

        BillingClient client = BillingClient.newBuilder(mContext)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener((billingResult, purchases) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (purchases != null && !purchases.isEmpty()) {
                        hideAds();
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user canceling the purchase flow.
                } else if (mView != null) {
                    mView.showMessage(billingResult.getDebugMessage());
                }
            })
            .build();
        client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                billingConnection = false;
                billingClient = null;
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                billingConnection = false;

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient = client;
                    callback.onBillingClientReady(client);
                } else if (mView != null) {
                    mView.showMessage(billingResult.getDebugMessage());
                }
            }
        });
    }

    private void hideAds() {
        mSettings.setAdsEnabled(false);
        if (mView != null) {
            mView.showMessage(R.string.ads_disabled);
            if (activity != null) {
                activity.startActivity(
                    new Intent(activity, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                );
                Runtime.getRuntime().exit(0);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void createBackup() {
        mCreateBackupInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(String backup) {
                        File dir = mContext.getFilesDir();
                        File file = writeToFile(dir, BACKUP_FILE_NAME, backup);
                        if (mView != null) {
                            mView.startExportFileActivity(file);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void onFileImport(String backup) {
        mImportBackupInteractor.execute(backup)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        if (mView != null) {
                            mView.showImportDialog();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.dismissImportDialog();
                            EventBusHelper.updateNoteList();
                            EventBusHelper.importCompleted();
                            EventBusHelper.recreate();
                            // fixme
                            EventBusHelper.showMessage(R.string.msg_import_success, 1000);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        if (mView != null) {
                            mView.dismissImportDialog();
                            mView.showMessage(R.string.error);
                        }
                    }
                });
    }

    @Override
    public void onPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case AppSettingsRepository.PREF_NIGHT_MODE:
                EventBusHelper.recreate();
                break;

            case AppSettingsRepository.PREF_SHOW_NOTES_CONTENT:
                EventBusHelper.updateNoteList();
                break;
        }
    }

}
