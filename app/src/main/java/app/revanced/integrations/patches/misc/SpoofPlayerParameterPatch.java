package app.revanced.integrations.patches.misc;

import static app.revanced.integrations.utils.ReVancedUtils.containsAny;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.whitelist.Whitelist;

public class SpoofPlayerParameterPatch {

    /**
     * Target player parameters.
     */
    private static final String[] PLAYER_PARAMETER_WHITELIST = {
            "YAHI", // Autoplay in feed
            "SAFg"  // Autoplay in scrim
    };

    /**
     * On app first start, the first video played usually contains a single non-default window setting value
     * and all other subtitle settings for the video are (incorrect) default shorts window settings.
     * For this situation, the shorts settings must be replaced.
     * <p>
     * But some videos use multiple text positions on screen (such as youtu.be/3hW1rMNC89o),
     * and by chance many of the subtitles uses window positions that match a default shorts position.
     * To handle these videos, selectively allowing the shorts specific window settings to 'pass thru' unchanged,
     * but only if the video contains multiple non-default subtitle window positions.
     * <p>
     * Do not enable 'pass thru mode' until this many non default subtitle settings are observed for a single video.
     */
    private static final int NUMBER_OF_NON_DEFAULT_SUBTITLES_BEFORE_ENABLING_PASSTHRU = 2;

    /**
     * Player parameters parameters used in autoplay in scrim
     * Prepend this parameter to mute video playback (for autoplay in feed)
     */
    private static final String PLAYER_PARAMETER_SCRIM = "SAFgAXgB";

    /**
     * Player parameters parameters used in shorts and stories.
     * Known issue: channel watermark is hidden.
     * Known issue: end screen cards are hidden.
     * Known issue: downloading videos may not work.
     * Known issue: Video may have playback at 1:00.
     */
    private static final String PLAYER_PARAMETER_SHORTS = "8AEB";

    /**
     * Player parameters used in incognito mode's visitor data.
     * Known issue: ambient mode may not work.
     * Known issue: downloading videos may not work.
     * Known issue: seekbar thumbnails are hidden.
     */
    private static final String PLAYER_PARAMETER_INCOGNITO = "CgIQBg==";

    /**
     * The number of non default subtitle settings encountered for the current video.
     */
    private static int numberOfNonDefaultSettingsObserved;
    @Nullable
    private static String currentVideoId;

    /**
     * Injection point.
     *
     * @param originalValue originalValue player parameter
     */
    public static String overridePlayerParameter(String originalValue) {
        try {
            if (Whitelist.isChannelADSWhitelisted() || !SettingsEnum.SPOOF_PLAYER_PARAMETER.getBoolean() || originalValue.startsWith(PLAYER_PARAMETER_SHORTS)) {
                return originalValue;
            }

            final String playerParameters = SettingsEnum.SPOOF_PLAYER_PARAMETER_TYPE.getBoolean()
                ? PLAYER_PARAMETER_INCOGNITO
                : PLAYER_PARAMETER_SHORTS;

            boolean isPlayingFeed = containsAny(originalValue, PLAYER_PARAMETER_WHITELIST) && PlayerType.getCurrent() == PlayerType.INLINE_MINIMAL;
            if (isPlayingFeed) {
                // Videos in feed won't autoplay with sound.
                return PLAYER_PARAMETER_SCRIM + playerParameters;
            } else {
                // Spoof the parameter to prevent playback issues.
                return playerParameters;
            }
        } catch (Exception ex) {
            LogHelper.printException(SpoofPlayerParameterPatch.class, "overrideProtobufParameter failure", ex);
        }

        return originalValue;
    }


    /**
     * Injection point.
     */
    public static boolean getSeekbarThumbnailOverrideValue(boolean original) {
        return (SettingsEnum.SPOOF_PLAYER_PARAMETER.getBoolean() && SettingsEnum.SPOOF_PLAYER_PARAMETER_TYPE.getBoolean()) || original;
    }

    /**
     * Injection point.
     *
     * @param view seekbar thumbnail view.  Includes both shorts and regular videos.
     */
    public static void seekbarImageViewCreated(ImageView view) {
        try {
            if (SettingsEnum.SPOOF_PLAYER_PARAMETER.getBoolean() && SettingsEnum.SPOOF_PLAYER_PARAMETER_TYPE.getBoolean()) {
                view.setVisibility(View.GONE);
                // Also hide the white border around the thumbnail (otherwise a 1 pixel wide bordered frame is visible).
                ViewGroup parentLayout = (ViewGroup) view.getParent();
                parentLayout.setPadding(0, 0, 0, 0);
            }
        } catch (Exception ex) {
            LogHelper.printException(SpoofPlayerParameterPatch.class, "seekbarImageViewCreated failure", ex);
        }
    }

    /**
     * Injection point.  Overrides values passed into SubtitleWindowSettings constructor.
     *
     * @param ap anchor position. A bitmask with 6 bit fields, that appears to indicate the layout position on screen
     * @param ah anchor horizontal. A percentage [0, 100], that appears to be a horizontal text anchor point
     * @param av anchor vertical. A percentage [0, 100], that appears to be a vertical text anchor point
     * @param vs appears to indicate if subtitles exist, and the value is always true.
     * @param sd function is not entirely clear
     */
    public static int[] getSubtitleWindowSettingsOverride(int ap, int ah, int av, boolean vs, boolean sd) {
        // Videos with custom captions that specify screen positions appear to always have correct screen positions (even with spoofing).
        // But for auto generated and most other captions, the spoof incorrectly gives various default Shorts caption settings.
        // Check for these known default shorts captions parameters, and replace with the known correct values.
        //
        // If a regular video uses a custom subtitle setting that match a default short setting,
        // then this will incorrectly replace the setting.
        // But, if the video uses multiple subtitles in different screen locations, then detect the non-default values
        // and do not replace any window settings for the video (regardless if they match a shorts default).
        if (SettingsEnum.SPOOF_PLAYER_PARAMETER.getBoolean() && !PlayerType.getCurrent().isNoneOrHidden()
                && numberOfNonDefaultSettingsObserved < NUMBER_OF_NON_DEFAULT_SUBTITLES_BEFORE_ENABLING_PASSTHRU) { // video is not a Short or Story
            for (SubtitleWindowReplacementSettings setting : SubtitleWindowReplacementSettings.values()) {
                if (setting.match(ap, ah, av, vs, sd))
                    return setting.replacementSetting();
            }

            numberOfNonDefaultSettingsObserved++;
        }

        return new int[]{ap, ah, av};
    }

    /**
     * Injection point.
     */
    public static void setCurrentVideoId(@NonNull String videoId) {
        try {
            if (videoId.equals(currentVideoId)) {
                return;
            }
            currentVideoId = videoId;
            numberOfNonDefaultSettingsObserved = 0;
        } catch (Exception ex) {
            LogHelper.printException(SpoofPlayerParameterPatch.class, "setCurrentVideoId failure", ex);
        }
    }


    /**
     * Known incorrect default Shorts subtitle parameters, and the corresponding correct (non-Shorts) values.
     */
    private enum SubtitleWindowReplacementSettings {
        DEFAULT_SHORTS_PARAMETERS_1(10, 50, 0, true, false,
                34, 50, 95),
        DEFAULT_SHORTS_PARAMETERS_2(9, 20, 0, true, false,
                34, 50, 90),
        DEFAULT_SHORTS_PARAMETERS_3(9, 20, 0, true, true,
                33, 20, 100);

        // original values
        final int ap, ah, av;
        final boolean vs, sd;

        // replacement int values
        final int[] replacement;

        SubtitleWindowReplacementSettings(int ap, int ah, int av, boolean vs, boolean sd,
                                          int replacementAp, int replacementAh, int replacementAv) {
            this.ap = ap;
            this.ah = ah;
            this.av = av;
            this.vs = vs;
            this.sd = sd;
            this.replacement = new int[]{replacementAp, replacementAh, replacementAv};
        }

        boolean match(int ap, int ah, int av, boolean vs, boolean sd) {
            return this.ap == ap && this.ah == ah && this.av == av && this.vs == vs && this.sd == sd;
        }

        int[] replacementSetting() {
            return replacement;
        }
    }
}
