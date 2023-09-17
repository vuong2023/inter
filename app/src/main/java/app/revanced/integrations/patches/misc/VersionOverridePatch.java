package app.revanced.integrations.patches.misc;

import static app.revanced.integrations.utils.SharedPrefHelper.SharedPrefNames.REVANCED;
import static app.revanced.integrations.utils.SharedPrefHelper.getBoolean;
import static app.revanced.integrations.utils.SharedPrefHelper.getString;

import app.revanced.integrations.utils.LogHelper;

public class VersionOverridePatch {

    /**
     * Context is overridden when trying to play a YouTube video from the Google Play Store,
     * Which is speculated to affect VersionOverridePatch
     */
    public static String getVersionOverride(String version) {

        try {
            return getBoolean(REVANCED, "revanced_spoof_app_version", false)
                    ? getString(REVANCED, "revanced_spoof_app_version_target", "18.20.39")
                    : version;
        } catch (Exception ex) {
            LogHelper.printException(VersionOverridePatch.class, "Failed to load getVersionOverride", ex);
            return version;
        }
    }
}
