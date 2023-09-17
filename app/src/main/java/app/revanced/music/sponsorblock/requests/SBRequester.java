package app.revanced.music.sponsorblock.requests;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.revanced.music.requests.Requester;
import app.revanced.music.settings.SettingsEnum;
import app.revanced.music.sponsorblock.SponsorBlockSettings;
import app.revanced.music.sponsorblock.objects.SegmentCategory;
import app.revanced.music.sponsorblock.objects.SponsorSegment;
import app.revanced.music.utils.LogHelper;
import app.revanced.music.utils.ReVancedUtils;

public class SBRequester {
    /**
     * TCP timeout
     */
    private static final int TIMEOUT_TCP_DEFAULT_MILLISECONDS = 7000;

    /**
     * HTTP response timeout
     */
    private static final int TIMEOUT_HTTP_DEFAULT_MILLISECONDS = 10000;

    /**
     * Response code of a successful API call
     */
    private static final int HTTP_STATUS_CODE_SUCCESS = 200;

    private SBRequester() {
    }


    @NonNull
    public static SponsorSegment[] getSegments(@NonNull String videoId) {
        ReVancedUtils.verifyOffMainThread();
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(videoId, SegmentCategory.sponsorBlockAPIFetchCategories);
            final int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                JSONArray responseArray = Requester.parseJSONArray(connection);
                final long minSegmentDuration = 0;
                for (int i = 0, length = responseArray.length(); i < length; i++) {
                    JSONObject obj = (JSONObject) responseArray.get(i);
                    JSONArray segment = obj.getJSONArray("segment");
                    final long start = (long) (segment.getDouble(0) * 1000);
                    final long end = (long) (segment.getDouble(1) * 1000);

                    String uuid = obj.getString("UUID");
                    final boolean locked = obj.getInt("locked") == 1;
                    String categoryKey = obj.getString("category");
                    SegmentCategory category = SegmentCategory.byCategoryKey(categoryKey);
                    if (category == null) {
                        LogHelper.printException(SBRequester.class, "Received unknown category: " + categoryKey); // should never happen
                    } else if ((end - start) >= minSegmentDuration) {
                        segments.add(new SponsorSegment(category, uuid, start, end, locked));
                    }
                }
                runVipCheckInBackgroundIfNeeded();
            } else if (responseCode == 404) {
                // no segments are found.  a normal response
                LogHelper.printDebug(SBRequester.class, "No segments found for video: " + videoId);
            } else {
                connection.disconnect(); // something went wrong, might as well disconnect
            }
        } catch (IOException ignored) {
        } catch (Exception ex) {
            // Should never happen
            LogHelper.printException(SBRequester.class, "getSegments failure", ex);
        }

        return segments.toArray(new SponsorSegment[0]);
    }

    public static void runVipCheckInBackgroundIfNeeded() {
        if (!SponsorBlockSettings.userHasSBPrivateId()) {
            return; // User cannot be a VIP. User has never voted, created any segments, or has imported a SB user id.
        }
        long now = System.currentTimeMillis();
        if (now < (SettingsEnum.SB_LAST_VIP_CHECK.getLong() + TimeUnit.DAYS.toMillis(3))) {
            return;
        }
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                SettingsEnum.SB_LAST_VIP_CHECK.saveValue(now);
            } catch (Exception ex) {
                LogHelper.printException(SBRequester.class, "Failed to check VIP", ex); // should never happen
            }
        });
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(String... params) throws IOException {
        HttpURLConnection connection = Requester.getConnectionFromRoute(SettingsEnum.SB_API_URL.getString(), SBRoutes.GET_SEGMENTS, params);
        connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
        connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);
        return connection;
    }
}
