package app.revanced.integrations.patches.button;

import static app.revanced.integrations.utils.ResourceUtils.anim;
import static app.revanced.integrations.utils.ResourceUtils.findView;
import static app.revanced.integrations.utils.ResourceUtils.integer;

import android.annotation.SuppressLint;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.VideoHelpers;

public class ExternalDownload {
    public static boolean isButtonEnabled;
    static WeakReference<ImageView> buttonView = new WeakReference<>(null);
    @SuppressLint("StaticFieldLeak")
    static ConstraintLayout constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    static boolean isShowing;
    static boolean isScrubbed;

    public static void initialize(Object obj) {
        try {
            constraintLayout = (ConstraintLayout) obj;
            isButtonEnabled = setValue();
            ImageView imageView = findView(ExternalDownload.class, constraintLayout, "external_download_button");

            imageView.setOnClickListener(view -> VideoHelpers.download(view.getContext()));
            buttonView = new WeakReference<>(imageView);

            fadeDurationFast = integer("fade_duration_fast");
            fadeDurationScheduled = integer("fade_duration_scheduled");

            fadeIn = anim("fade_in");
            fadeIn.setDuration(fadeDurationFast);

            fadeOut = anim("fade_out");
            fadeOut.setDuration(fadeDurationScheduled);

            isShowing = true;
            isScrubbed = false;
            changeVisibility(false);

        } catch (Exception e) {
            LogHelper.printException(ExternalDownload.class, "Unable to set FrameLayout", e);
        }
    }

    public static void changeVisibility(boolean currentVisibility) {
        ImageView imageView = buttonView.get();

        if (isShowing == currentVisibility || constraintLayout == null || imageView == null) return;

        isShowing = currentVisibility;

        if (isScrubbed && isButtonEnabled) {
            isScrubbed = false;
            imageView.setVisibility(View.VISIBLE);
            return;
        }

        if (currentVisibility && isButtonEnabled) {
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(fadeIn);
        } else if (imageView.getVisibility() == View.VISIBLE) {
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }

    public static void changeVisibilityNegatedImmediate(boolean isUserScrubbing) {
        ImageView imageView = buttonView.get();

        if (constraintLayout == null || imageView == null || !isUserScrubbing) return;

        isShowing = false;
        isScrubbed = true;
        imageView.setVisibility(View.GONE);
    }

    public static void refreshVisibility() {
        isButtonEnabled = setValue();
    }

    private static boolean setValue() {
        return SettingsEnum.OVERLAY_BUTTON_EXTERNAL_DOWNLOADER.getBoolean();
    }
}

