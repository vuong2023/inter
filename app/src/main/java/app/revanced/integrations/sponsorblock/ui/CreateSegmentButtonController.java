package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ResourceUtils.anim;
import static app.revanced.integrations.utils.ResourceUtils.identifier;
import static app.revanced.integrations.utils.ResourceUtils.integer;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.patches.video.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ResourceType;

public class CreateSegmentButtonController {
    private static WeakReference<ImageView> buttonReference = new WeakReference<>(null);

    private static Animation fadeIn;
    private static Animation fadeOut;
    private static boolean isShowing;
    private static boolean isScrubbed;


    /**
     * injection point
     */
    public static void initialize(Object viewStub) {
        try {
            RelativeLayout controlsLayout = (RelativeLayout) viewStub;
            ImageView imageView = Objects.requireNonNull(controlsLayout.findViewById(
                    identifier("sb_sponsorblock_button", ResourceType.ID)));

            imageView.setOnClickListener(v -> SponsorBlockViewController.toggleNewSegmentLayoutVisibility());
            buttonReference = new WeakReference<>(imageView);

            // Animations
            if (fadeIn == null) {
                fadeIn = anim("fade_in");
                fadeIn.setDuration(integer("fade_duration_fast"));
                fadeOut = anim("fade_out");
                fadeOut.setDuration(integer("fade_duration_scheduled"));
            }
            isShowing = true;
            isScrubbed = false;
            changeVisibilityImmediate(false);
        } catch (Exception ex) {
            LogHelper.printException(CreateSegmentButtonController.class, "Unable to set RelativeLayout", ex);
        }
    }

    public static void changeVisibilityImmediate(boolean visible) {
        changeVisibility(visible, true);
    }

    /**
     * injection point
     */
    public static void changeVisibilityNegatedImmediate(boolean isUserScrubbing) {
        ImageView imageView = buttonReference.get();

        if (imageView == null || !isUserScrubbing) return;

        isShowing = false;
        isScrubbed = true;
        imageView.setVisibility(View.GONE);
    }

    /**
     * injection point
     */
    public static void changeVisibility(boolean visible) {
        changeVisibility(visible, false);
    }

    public static void changeVisibility(boolean visible, boolean immediate) {
        try {
            if (isShowing == visible) return;
            isShowing = visible;

            ImageView iView = buttonReference.get();
            if (iView == null) return;

            if (visible) {
                iView.clearAnimation();
                if (!shouldBeShown()) {
                    return;
                }
                if (!immediate) {
                    iView.startAnimation(fadeIn);
                }
                iView.setVisibility(View.VISIBLE);
                return;
            }

            if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (!immediate) {
                    iView.startAnimation(fadeOut);
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            LogHelper.printException(CreateSegmentButtonController.class, "changeVisibility failure", ex);
        }
    }

    private static boolean shouldBeShown() {
        return SettingsEnum.SB_ENABLED.getBoolean() && SettingsEnum.SB_CREATE_NEW_SEGMENT.getBoolean()
                && !VideoInformation.isAtEndOfVideo();
    }
}
