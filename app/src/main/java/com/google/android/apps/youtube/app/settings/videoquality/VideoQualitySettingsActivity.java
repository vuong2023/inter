package com.google.android.apps.youtube.app.settings.videoquality;

import static app.revanced.integrations.utils.ResourceUtils.identifier;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import app.revanced.integrations.settingsmenu.ReVancedSettingsFragment;
import app.revanced.integrations.settingsmenu.ReturnYouTubeDislikeSettingsFragment;
import app.revanced.integrations.settingsmenu.SponsorBlockSettingsFragment;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ResourceHelper;
import app.revanced.integrations.utils.ResourceType;
import app.revanced.integrations.utils.ThemeHelper;

/**
 * @noinspection ALL
 */
public class VideoQualitySettingsActivity extends Activity {

    public static ImageButton getImageButton(ViewGroup viewGroup) {
        if (viewGroup == null) return null;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ImageButton) {
                return (ImageButton) childAt;
            }
        }
        return null;
    }

    public static TextView getTextView(ViewGroup viewGroup) {
        if (viewGroup == null) return null;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof TextView) {
                return (TextView) childAt;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        setTheme(ThemeHelper.getSettingTheme());

        super.onCreate(bundle);
        setContentView(identifier("revanced_settings_with_toolbar", ResourceType.LAYOUT));
        initImageButton();

        switch (getIntent().getDataString()) {
            case "sponsorblock_settings" -> {
                trySetTitle(identifier("revanced_sponsorblock_settings_title", ResourceType.STRING));
                getFragmentManager()
                        .beginTransaction()
                        .replace(identifier("revanced_settings_fragments", ResourceType.ID), new SponsorBlockSettingsFragment())
                        .commit();
            }
            case "ryd_settings" -> {
                trySetTitle(identifier("revanced_ryd_settings_title", ResourceType.STRING));
                getFragmentManager()
                        .beginTransaction()
                        .replace(identifier("revanced_settings_fragments", ResourceType.ID), new ReturnYouTubeDislikeSettingsFragment())
                        .commit();
            }
            case "extended_settings" -> {
                trySetTitle(identifier("revanced_extended_settings_title", ResourceType.STRING));
                getFragmentManager()
                        .beginTransaction()
                        .replace(identifier("revanced_settings_fragments", ResourceType.ID), new ReVancedSettingsFragment())
                        .commit();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void trySetTitle(int i) {
        try {
            getTextView(findViewById(identifier("toolbar", ResourceType.ID))).setText(i);
        } catch (Exception e) {
            LogHelper.printException(VideoQualitySettingsActivity.class, "Couldn't set Toolbar title", e);
        }
    }

    private void initImageButton() {
        try {
            ImageButton imageButton = getImageButton(findViewById(identifier("toolbar", ResourceType.ID)));
            imageButton.setOnClickListener(view -> VideoQualitySettingsActivity.this.onBackPressed());
            imageButton.setImageDrawable(getResources().getDrawable(ResourceHelper.getArrow()));
        } catch (Exception e) {
            LogHelper.printException(VideoQualitySettingsActivity.class, "Couldn't set Toolbar click handler", e);
        }
    }
}