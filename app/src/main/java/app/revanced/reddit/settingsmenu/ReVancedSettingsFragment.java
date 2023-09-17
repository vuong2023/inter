package app.revanced.reddit.settingsmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.reddit.settings.SettingsEnum;
import app.revanced.reddit.settings.SharedPrefCategory;
import app.revanced.reddit.utils.LogHelper;

/**
 * @noinspection ALL
 */
public class ReVancedSettingsFragment extends PreferenceFragment {

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            SettingsEnum setting = SettingsEnum.settingFromPath(str);
            if (setting == null)
                return;

            if (setting.rebootApp)
                rebootDialog(getActivity());

        } catch (Exception ex) {
            LogHelper.printException(ReVancedSettingsFragment.class, "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    private static void reboot(@NonNull Activity activity) {
        Intent restartIntent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());

        activity.finishAffinity();
        activity.startActivity(restartIntent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefCategory.REDDIT.prefName);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        final Activity activity = getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(activity);
        setPreferenceScreen(preferenceScreen);

        addPreferences(activity, preferenceScreen, SettingsStatus.generalAds, SettingsEnum.HIDE_COMMENT_ADS);
        addPreferences(activity, preferenceScreen, SettingsStatus.generalAds, SettingsEnum.HIDE_OLD_POST_ADS);
        addPreferences(activity, preferenceScreen, SettingsStatus.generalAds, SettingsEnum.HIDE_NEW_POST_ADS);
        addPreferences(activity, preferenceScreen, SettingsStatus.screenshotPopup, SettingsEnum.DISABLE_SCREENSHOT_POPUP);
        addPreferences(activity, preferenceScreen, SettingsStatus.navigationButtons, SettingsEnum.HIDE_CHAT_BUTTON);
        addPreferences(activity, preferenceScreen, SettingsStatus.navigationButtons, SettingsEnum.HIDE_CREATE_BUTTON);
        addPreferences(activity, preferenceScreen, SettingsStatus.navigationButtons, SettingsEnum.HIDE_DISCOVER_BUTTON);
        addPreferences(activity, preferenceScreen, SettingsStatus.placeButton, SettingsEnum.HIDE_PLACE_BUTTON);
        addPreferences(activity, preferenceScreen, SettingsStatus.openLinksDirectly, SettingsEnum.OPEN_LINKS_DIRECTLY);
        addPreferences(activity, preferenceScreen, SettingsStatus.openLinksExternally, SettingsEnum.OPEN_LINKS_EXTERNALLY);
        addPreferences(activity, preferenceScreen, SettingsStatus.sanitizeUrlQuery, SettingsEnum.SANITIZE_URL_QUERY);
    }

    private void addPreferences(Activity activity, PreferenceScreen preferenceScreen, boolean isAvailable, SettingsEnum setting) {
        if (!isAvailable) return;

        SwitchPreference switchPreference = new SwitchPreference(activity);
        switchPreference.setChecked(setting.getBoolean());
        switchPreference.setTitle(setting.getTitle());
        switchPreference.setSummary(setting.getSummary());
        switchPreference.setOnPreferenceChangeListener((pref, newValue) -> {
            setting.saveValue(newValue);
            return true;
        });
        preferenceScreen.addPreference(switchPreference);
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    private void rebootDialog(@NonNull Activity activity) {
        new AlertDialog.Builder(activity).
                setMessage("Refresh and restart").
                setPositiveButton("RESTART", (dialog, i) -> reboot(activity))
                .setNegativeButton("CANCEL", null)
                .show();
    }
}
