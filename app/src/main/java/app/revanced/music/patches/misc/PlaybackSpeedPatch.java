package app.revanced.music.patches.misc;

import static app.revanced.music.utils.ReVancedUtils.showToastShort;
import static app.revanced.music.utils.StringRef.str;

import app.revanced.music.settings.SettingsEnum;

public class PlaybackSpeedPatch {
    private static float selectedSpeed = 1.0f;

    public static float getPlaybackSpeed() {
        try {
            return SettingsEnum.DEFAULT_PLAYBACK_SPEED.getFloat();
        } catch (Exception ignored) {
        }
        return selectedSpeed;
    }

    public static void overrideSpeed(final float speedValue) {
        if (speedValue != selectedSpeed)
            selectedSpeed = speedValue;
    }

    public static void userChangedSpeed(final float speed) {
        selectedSpeed = speed;

        if (SettingsEnum.ENABLE_SAVE_PLAYBACK_SPEED.getBoolean()) {
            SettingsEnum.DEFAULT_PLAYBACK_SPEED.saveValue(speed);
            showToastShort(str("revanced_save_playback_speed") + "\u2009" + speed + "x");
        }
    }
}
