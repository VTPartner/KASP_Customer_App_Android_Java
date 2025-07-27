package com.kapstranspvtltd.kaps.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.SplashScreenActivity;
import com.kapstranspvtltd.kaps.activities.CustomerEditProfileActivity;
import com.kapstranspvtltd.kaps.activities.EnterReferralActivity;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.InviteEarnActivity;
import com.kapstranspvtltd.kaps.activities.LoginActivity;
import com.kapstranspvtltd.kaps.activities.WalletActivity;
import com.kapstranspvtltd.kaps.coins.CoinsHomeScreenActivity;
import com.kapstranspvtltd.kaps.common_activities.ScheduledBookingsActivity;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.bookings.AllDriverBookingsAndOrdersActivity;
import com.kapstranspvtltd.kaps.handyman_customer_app.activities.bookings.AllHandymanBookingsAndOrdersActivity;
import com.kapstranspvtltd.kaps.jcb_crane_customer_app.activities.bookings.AllJcbCraneBookingsAndOrdersActivity;
import com.kapstranspvtltd.kaps.language_change_utils.BaseFragment;
import com.kapstranspvtltd.kaps.language_change_utils.LocaleHelper;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.model.AppContent;
import com.kapstranspvtltd.kaps.utility.AppContentManager;
import com.kapstranspvtltd.kaps.databinding.FragmentAccountSettingsBinding;


public class AccountSettingsFragment extends BaseFragment {

    FragmentAccountSettingsBinding binding;

    PreferenceManager preferenceManager;


    public AccountSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false);
        initView();
        setupClickListeners();
        loadDynamicContent();
        return binding.getRoot();
    }

    private void setupClickListeners() {
        binding.txtDeleteAccount.setOnClickListener(v->showDeleteAccountDialog());
        binding.txtLogout.setOnClickListener(v -> showLogoutDialog());
        binding.txtTermsConditions.setOnClickListener(v ->  openWebUrl("https://www.kaps9.in/terms&conditions"));
        binding.txtCall.setOnClickListener(v->handleCallClick());
        binding.txtLaunge.setOnClickListener(v->showLanguageDialog());
        binding.txtEdit.setOnClickListener(v->goToEditProfilePage());
        binding.txtWallet.setOnClickListener(v->goToWalletPage());
        binding.txtDriversRides.setOnClickListener(v->goToAllDriversRidesPage());
        binding.txtScheduledBookings.setOnClickListener(v->goToAllScheduledPage());
        binding.txtJcbCraneRide.setOnClickListener(v->goToAllJcbCraneRidesPage());
        binding.txtHandymanRides.setOnClickListener(v->goToAllHandymanRidesPage());
        binding.enjoyearnings.setOnClickListener(v->goToWalletPage());
        binding.rewardCoins.setOnClickListener(v->goToCoinHomePage());
        binding.inviteFriends.setOnClickListener(v->openToShareAppInviteLink());
        binding.txtAddRefferalCode.setOnClickListener(v->goToAddRefferalCode());
    }

    private void goToAddRefferalCode() {
        Intent intent = new Intent(getActivity(), EnterReferralActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void goToCoinHomePage() {
        Intent intent = new Intent(getActivity(), CoinsHomeScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void goToAllScheduledPage() {
        Intent intent = new Intent(getActivity(), ScheduledBookingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void openToShareAppInviteLink() {
        Intent intent = new Intent(getActivity(), InviteEarnActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);

        /*String appUrl = "https://play.google.com/store/apps/details?id=com.kapstranspvtltd.kaps&hl=en_IN";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out KAPS App!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Experience seamless transportation and on-demand services with the KAPS app! \uD83D\uDE80 Whether you need reliable Goods Delivery, Cab Booking, JCB & Crane Services, Professional Drivers, or skilled Handyman Services, KAPS has you covered. Download now and simplify your daily needs with just a few taps! \uD83D\uDD17 Get the app here: " + appUrl);

        startActivity(Intent.createChooser(shareIntent, "Share via"));

         */
    }


    private void goToHomePage() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void goToAllJcbCraneRidesPage() {
        Intent intent = new Intent(getActivity(), AllJcbCraneBookingsAndOrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void goToAllHandymanRidesPage() {
        Intent intent = new Intent(getActivity(), AllHandymanBookingsAndOrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void goToAllDriversRidesPage() {
        Intent intent = new Intent(getActivity(), AllDriverBookingsAndOrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }


    private void goToEditProfilePage() {
        Intent intent = new Intent(getActivity(), CustomerEditProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }

    private void goToWalletPage() {
        Intent intent = new Intent(getActivity(), WalletActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
    }
    private void showLanguageDialog() {
        final String[] languages = {"English", "हिंदी", "मराठी", "ಕನ್ನಡ"};
        final String[] languageCodes = {"en", "hi", "mr", "kn"};

        new MaterialAlertDialogBuilder(getActivity())
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, getCurrentLanguagePosition(languageCodes), (dialog, which) -> {
                    // Change app language
                    LocaleHelper.setLocale(getActivity(), languageCodes[which]);

                    // Restart app to apply changes
                    Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .show();
    }

    private int getCurrentLanguagePosition(String[] languageCodes) {
        String currentLanguage = LocaleHelper.getLanguage(getActivity());
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                return i;
            }
        }
        return 0; // Default to English
    }

    private void handleCallClick() {
        try {

            String driverMobileNo = "+919665141555";

            // Create the intent to make a call
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + driverMobileNo));

            // Check if there's an app that can handle this intent
            if (callIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(callIntent);
            } else {
//                showError("No app available to make calls");
            }

        } catch (Exception e) {
            e.printStackTrace();
//            showError("Unable to make call");
        }
    }


    private void initView() {
        preferenceManager = new PreferenceManager(requireContext());
        String customerId = preferenceManager.getStringValue("customer_id");
        String customerName = preferenceManager.getStringValue("customer_name");
        String customerMobileNo = preferenceManager.getStringValue("customer_mobile_no");
        binding.txtCustomerName.setText(customerName);
        binding.txtCustomerPhone.setText(customerMobileNo);
        binding.txtCustomerId.setText("Customer ID #"+customerId);
    }

    private void openWebUrl(String url) {
        try {
            // Show loading if needed
            // progressDialog.show();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));

            // Verify that there's an app to handle this intent
            if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                Toast.makeText(getActivity(), "No web browser found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Unable to open website", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show confirmation dialog
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Request Sent")
                            .setMessage("Your account deletion request has been sent to our team. " +
                                    "We will process it within 24-48 hours.")
                            .setPositiveButton("OK", (innerDialog, innerWhich) -> {
                                // Optional: Send API request to backend about deletion request
//                                sendDeleteAccountRequest();
                            })
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear all preferences
                    clearAllUserData();

                    // Navigate to DriverTypeActivity
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllUserData() {
        // Clear all relevant preferences
        preferenceManager.clearPreferences();
    }

    private void loadDynamicContent() {
        // Load invite friends content
        AppContent inviteContent = AppContentManager.getInstance(requireContext())
                .getFirstContentForScreen("invite_friends");
        
        if (inviteContent != null) {
            // Set invite friends text
            if (binding.inviteFriendsText != null && !inviteContent.getTitle().equals("NA")) {
                binding.inviteFriendsText.setText(inviteContent.getTitle());
            }
            
            // Set invite friends image
            if (binding.inviteFriendsImage != null && !inviteContent.getImageUrl().equals("NA")) {
                if (inviteContent.getImageUrl().startsWith("http")) {
                    com.bumptech.glide.Glide.with(this)
                            .load(inviteContent.getImageUrl())
                            .placeholder(R.drawable.invite_friends)
                            .error(R.drawable.invite_friends)
                            .into(binding.inviteFriendsImage);
                } else {
                    try {
                        int resourceId = getResources().getIdentifier(
                                inviteContent.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                requireContext().getPackageName()
                        );
                        if (resourceId != 0) {
                            binding.inviteFriendsImage.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        binding.inviteFriendsImage.setImageResource(R.drawable.invite_friends);
                    }
                }
            }
        }

        // Load KAPS coins content 
        AppContent coinsContent = AppContentManager.getInstance(requireContext())
                .getFirstContentForScreen("kaps_coin"); 
        
        if (coinsContent != null) {
            // Set KAPS coins text
            if (binding.rewardCoinsText != null && !coinsContent.getTitle().equals("NA")) {
                binding.rewardCoinsText.setText(coinsContent.getTitle());
            }
            
            // Set KAPS coins image
            if (binding.rewardCoinsImage != null && !coinsContent.getImageUrl().equals("NA")) {
                if (coinsContent.getImageUrl().startsWith("http")) {
                    com.bumptech.glide.Glide.with(this)
                            .load(coinsContent.getImageUrl())
                            .placeholder(R.drawable.ic_coins_stack)
                            .error(R.drawable.ic_coins_stack)
                            .into(binding.rewardCoinsImage);
                } else {
                    try {
                        int resourceId = getResources().getIdentifier(
                                coinsContent.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                requireContext().getPackageName()
                        );
                        if (resourceId != 0) {
                            binding.rewardCoinsImage.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        binding.rewardCoinsImage.setImageResource(R.drawable.ic_coins_stack);
                    }
                }
            }
        }

        // Load enjoy earnings content 
        AppContent earningsContent = AppContentManager.getInstance(requireContext())
                .getFirstContentForScreen("enjoy_your_earning"); // Using same content for now
        
        if (earningsContent != null) {
            // Set enjoy earnings text
            if (binding.enjoyearningsText != null && !earningsContent.getTitle().equals("NA")) {
                binding.enjoyearningsText.setText(earningsContent.getTitle());
            }
            
            // Set enjoy earnings image
            if (binding.enjoyearningsImage != null && !earningsContent.getImageUrl().equals("NA")) {
                if (earningsContent.getImageUrl().startsWith("http")) {
                    com.bumptech.glide.Glide.with(this)
                            .load(earningsContent.getImageUrl())
                            .placeholder(R.drawable.earn_money)
                            .error(R.drawable.earn_money)
                            .into(binding.enjoyearningsImage);
                } else {
                    try {
                        int resourceId = getResources().getIdentifier(
                                earningsContent.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                requireContext().getPackageName()
                        );
                        if (resourceId != 0) {
                            binding.enjoyearningsImage.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        binding.enjoyearningsImage.setImageResource(R.drawable.earn_money);
                    }
                }
            }
        }
    }
}