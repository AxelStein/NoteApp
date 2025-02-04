package com.axel_stein.noteapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.axel_stein.data.AppSettingsRepository;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BillingManager implements PurchasesUpdatedListener, BillingClientStateListener {

    public static final String PRODUCT_DISABLE_ADS = "disable_ads";

    private final Context context;
    private BillingClient billingClient;
    private final ArrayList<ProductDetails> availableProducts = new ArrayList<>();
    private final ArrayList<Purchase> purchases = new ArrayList<>();
    private final AppSettingsRepository settings;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public BillingManager(Context context, AppSettingsRepository settings) {
        this.context = context;
        this.settings = settings;
    }

    @NonNull
    private BillingClient createBillingClient() {
        return BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(this)
            .build();
    }

    public void onStop() {
        Timber.d("endConnection");
        billingClient.endConnection();
        billingClient = null;
    }

    public void onStart() {
        if (billingClient == null) {
            Timber.d("startConnection");
            billingClient = createBillingClient();
            billingClient.startConnection(this);
        }
    }

    private void queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            (billingResult, list) -> runOnUiThread(() -> onPurchasesUpdated(billingResult, list))
        );
    }

    private void queryProducts() {
        Timber.d("queryProducts");
        ArrayList<QueryProductDetailsParams.Product> products = new ArrayList<>();
        products.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_DISABLE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        );

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build(),
            (billingResult, list) -> {
                runOnUiThread(() -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Timber.d("availableProducts=%s", list);
                        availableProducts.clear();
                        availableProducts.addAll(list);
                    } else {
                        Timber.e("queryProducts failure=%s", billingResult.getDebugMessage());
                    }
                });
            }
        );
    }

    @NonNull
    public ArrayList<ProductDetails> getAvailableProducts() {
        return availableProducts;
    }

    public void print(Context ctx) {
        StringBuilder builder = new StringBuilder();
        for (Purchase purchase : purchases) {
            builder.append(purchase.toString());
            builder.append('\n');
        }

        if (builder.length() == 0) {
            Toast.makeText(ctx, "Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());

        PackageManager pm = ctx.getPackageManager();
        if (pm != null) {
            if (intent.resolveActivity(pm) != null) {
                ctx.startActivity(intent);
            } else {
                Log.e("TAG", "share note: no activity found");
                Toast.makeText(ctx, R.string.error_share, Toast.LENGTH_SHORT);
            }
        }
    }

    public void revoke() {
        Purchase current = null;
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                current = purchase;
                break;
            }
        }
        if (current == null) return;

        ConsumeParams params = ConsumeParams.newBuilder()
            .setPurchaseToken(current.getPurchaseToken())
            .build();

        billingClient.consumeAsync(
            params,
            (billingResult, purchaseToken) -> {
                runOnUiThread(() -> {
                    Timber.d("revoke=%s", billingResult);
                });
            });
    }

    @Nullable
    public BillingResult purchase(@NonNull Activity activity, @NonNull String productId) {
        ProductDetails product = null;
        for (ProductDetails p : availableProducts) {
            if (TextUtils.equals(productId, p.getProductId())) {
                product = p;
                break;
            }
        }

        if (product == null) return null;

        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParams = ImmutableList.of(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .build()
        );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParams)
            .build();

        return billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        Timber.d("onPurchasesUpdated result=%s list=%s", billingResult, list);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            purchases.clear();
            if (list == null) {
                settings.setAdsEnabled(true);
                return;
            }
            purchases.addAll(list);

            Purchase disableAds = null;
            for (Purchase purchase : list) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    List<String> products = purchase.getProducts();
                    if (products.contains(PRODUCT_DISABLE_ADS)) {
                        disableAds = purchase;
                        break;
                    }
                }
            }

            if (disableAds == null) {
                settings.setAdsEnabled(true);
            } else {
                if (disableAds.isAcknowledged()) {
                    disableAds();
                } else {
                    acknowledgePurchase(disableAds.getPurchaseToken());
                }
            }
        }
    }

    private void acknowledgePurchase(String token) {
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(token)
            .build();
        billingClient.acknowledgePurchase(
            params,
            billingResult -> {
                Timber.d("acknowledgePurchase=%s", billingResult);
                runOnUiThread(() -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        disableAds();
                    }
                });
            }
        );
    }

    private void disableAds() {
        boolean adsEnabled = settings.adsEnabled();
        settings.setAdsEnabled(false);

        if (adsEnabled) {
            Toast.makeText(context, R.string.ads_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        Timber.d("onBillingServiceDisconnected");

        runOnUiThread(() -> billingClient = null);
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        Timber.d("onBillingSetupFinished=%s", billingResult);
        runOnUiThread(() -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                queryProducts();
                queryPurchases();
            } else {
                billingClient = null;
            }
        });
    }

    private void runOnUiThread(Runnable task) {
        handler.post(task);
    }
}
