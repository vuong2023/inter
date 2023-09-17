package app.revanced.integrations.patches.ads;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;

final class QuickActionFilter extends Filter {
    public QuickActionFilter() {
        pathFilterGroups.addAll(
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS,
                        "quick_actions.eml|"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_LIKE_BUTTON,
                        "|like_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_DISLIKE_BUTTON,
                        "dislike_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_RELATED_VIDEO,
                        "fullscreen_related_videos"
                )
        );

        protobufBufferFilterGroups.addAll(
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_LIKE_BUTTON,
                        "yt_outline_thumb_up"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_DISLIKE_BUTTON,
                        "yt_outline_thumb_down"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_COMMENT_BUTTON,
                        "yt_outline_message_bubble_right"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_LIVE_CHAT_BUTTON,
                        "yt_outline_message_bubble_overlap"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_PLAYLIST_BUTTON,
                        "yt_outline_library_add"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_SHARE_BUTTON,
                        "yt_outline_share"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_QUICK_ACTIONS_MORE_BUTTON,
                        "yt_outline_overflow_horizontal"
                )
        );
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (path.startsWith("quick_actions.eml|") && !SettingsEnum.HIDE_FULLSCREEN_PANELS.getBoolean())
            return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

        return false;
    }
}
