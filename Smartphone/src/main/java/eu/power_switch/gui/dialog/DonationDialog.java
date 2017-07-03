/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.google_play_services.playstore.Base64;
import eu.power_switch.google_play_services.playstore.IabHelper;
import eu.power_switch.google_play_services.playstore.IabResult;
import eu.power_switch.google_play_services.playstore.Inventory;
import eu.power_switch.google_play_services.playstore.Purchase;
import eu.power_switch.google_play_services.playstore.SkuDetails;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import timber.log.Timber;

/**
 * Dialog to select a Play Store Donation
 * <p/>
 * Created by Markus on 01.10.2015.
 */
public class DonationDialog extends EventBusSupportDialogFragment {

    public static IabHelper iapHelper;

    @BindView(R.id.button_donate_10)
    Button       donate10;
    @BindView(R.id.button_donate_5)
    Button       donate5;
    @BindView(R.id.button_donate_2)
    Button       donate2;
    @BindView(R.id.button_donate_1)
    Button       donate1;
    @BindView(R.id.layout_donate_buttons)
    LinearLayout layoutDonationButtons;
    @BindView(R.id.layoutLoading)
    LinearLayout layoutLoading;

    private static final String SKU_DONATE_10 = "donate_10";
    private static final String SKU_DONATE_5  = "donate_5";
    private static final String SKU_DONATE_2  = "donate_2";
    private static final String SKU_DONATE_1  = "donate_1";

    private static final String SKU_TEST_PURCHASED        = "android.test.purchased";
    private static final String SKU_TEST_CANCELED         = "android.test.canceled";
    private static final String SKU_TEST_REFUNDED         = "android.test.refunded";
    private static final String SKU_TEST_ITEM_UNAVAILABLE = "android.test.item_unavailable";

    private static final List<String> IAP_IDS_LIST = Arrays.asList(SKU_DONATE_10, SKU_DONATE_5, SKU_DONATE_2, SKU_DONATE_1);

    private static final int requestCode = 123;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_donate_10:
                        initiatePurchase(SKU_DONATE_10);
//                        initiatePurchase(SKU_TEST_PURCHASED);
                        break;
                    case R.id.button_donate_5:
                        initiatePurchase(SKU_DONATE_5);
//                        initiatePurchase(SKU_TEST_CANCELED);
                        break;
                    case R.id.button_donate_2:
                        initiatePurchase(SKU_DONATE_2);
//                        initiatePurchase(SKU_TEST_REFUNDED);
                        break;
                    case R.id.button_donate_1:
                        initiatePurchase(SKU_DONATE_1);
//                        initiatePurchase(SKU_TEST_ITEM_UNAVAILABLE);
                        break;
                    default:
//                        initiatePurchase(SKU_TEST_PURCHASED);
                        break;
                }
            }
        };

        donate10.setOnClickListener(onClickListener);
        donate5.setOnClickListener(onClickListener);
        donate2.setOnClickListener(onClickListener);
        donate1.setOnClickListener(onClickListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle(getString(R.string.donate));
        builder.setNeutralButton(R.string.close, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.show();

        return dialog;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_donation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String key                    = SmartphonePreferencesHandler.getPublicKeyString();
            String base64EncodedPublicKey = new String(Base64.decode(key));
            iapHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

            iapHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        StatusMessageHandler.showInfoMessage(getContext(), "Error consuming: " + result.getMessage(), Snackbar.LENGTH_LONG);
                        Timber.e("Problem setting up In-app Billing: " + result);
                        dismiss();
                        return;
                    }
                    // Hooray, IAB is fully set up!

                    iapHelper.queryInventoryAsync(true, IAP_IDS_LIST, new IabHelper.QueryInventoryFinishedListener() {
                        @Override
                        public void onQueryInventoryFinished(final IabResult result, final Inventory inventory) {
                            if (result.isFailure()) {
                                // handle error
                                StatusMessageHandler.showInfoMessage(getContext(), "Error consuming: " + result.getMessage(), Snackbar.LENGTH_LONG);
                                dismiss();
                                return;
                            }

                            final SkuDetails skuDetails10 = inventory.getSkuDetails(SKU_DONATE_10);
                            final SkuDetails skuDetails5  = inventory.getSkuDetails(SKU_DONATE_5);
                            final SkuDetails skuDetails2  = inventory.getSkuDetails(SKU_DONATE_2);
                            final SkuDetails skuDetails1  = inventory.getSkuDetails(SKU_DONATE_1);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    donate10.setText(skuDetails10.getPrice());
                                    donate5.setText(skuDetails5.getPrice());
                                    donate2.setText(skuDetails2.getPrice());
                                    donate1.setText(skuDetails1.getPrice());

                                    layoutLoading.setVisibility(View.GONE);
                                    layoutDonationButtons.setVisibility(View.VISIBLE);
                                }
                            });

                            consumePreviousPurchases();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Timber.e(e);
            e.printStackTrace();
        }
    }

    private void initiatePurchase(String skuId) {
        IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                // check the returned data signature, the orderId, and the developerPayload string
                // in the Purchase object to make sure that you are getting the expected values.
                // You should verify that the orderId is a unique value that you have not previously processed,
                // and the developerPayload string matches the token that you sent previously with the purchase request.
                // As a further security precaution, you should perform the verification on your own secure server.

                if (result.isFailure()) {
                    StatusMessageHandler.showInfoMessage(getContext(), "Error purchasing: " + result.getMessage(), Snackbar.LENGTH_LONG);
                    return;
                }

                consumePurchase(purchase);

                StatusMessageHandler.showInfoMessage(getContext(), R.string.thank_you, Snackbar.LENGTH_LONG);
                getDialog().cancel();
            }
        };
        SecureRandom random = new SecureRandom();

        String requestString = new BigInteger(130, random).toString(32);
        iapHelper.launchPurchaseFlow(getActivity(), skuId, requestCode, purchaseFinishedListener, requestString);
    }

    private void consumePurchase(Purchase purchase) {
        iapHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                if (result.isFailure()) {
                    StatusMessageHandler.showInfoMessage(getContext(), "Error consuming: " + result.getMessage(), Snackbar.LENGTH_LONG);
                }
            }
        });
    }

    private void consumePreviousPurchases() {
        iapHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (result.isFailure()) {
                    StatusMessageHandler.showInfoMessage(getContext(), "Error purchasing: " + result.getMessage(), Snackbar.LENGTH_LONG);
                    return;
                }

                ArrayList<Purchase> purchases = new ArrayList<>();
                for (String skuId : IAP_IDS_LIST) {
                    if (inv.hasPurchase(skuId)) {
                        purchases.add(inv.getPurchase(skuId));
                    }
                }

                iapHelper.consumeAsync(purchases, new IabHelper.OnConsumeMultiFinishedListener() {
                    @Override
                    public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
                        for (IabResult r : results) {
                            if (r.isFailure()) {
                                StatusMessageHandler.showInfoMessage(getContext(), "Error consuming: " + r.getMessage(), Snackbar.LENGTH_LONG);
                                return;
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (iapHelper != null) {
            try {
                iapHelper.dispose();
            } catch (Exception e) {
                Timber.e("Error disposing In-App purchase helper", e);
            }
        }
        iapHelper = null;
    }
}
