package app.revanced.music.patches.utils;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Objects;

import app.revanced.music.utils.LogHelper;
import app.revanced.music.utils.ReVancedUtils;

/**
 * Hooking class for the current playing video.
 */
public final class VideoInformation {
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerControllerRef;
    private static Method seekMethod;

    @NonNull
    private static String videoId = "";

    private static long videoLength = 0;
    private static long videoTime = -1;

    /**
     * Injection point.
     *
     * @param playerController player controller object.
     */
    public static void initialize(@NonNull Object playerController) {
        try {
            playerControllerRef = new WeakReference<>(Objects.requireNonNull(playerController));
            videoTime = -1;
            videoLength = 0;

            seekMethod = playerController.getClass().getMethod(SEEK_METHOD_NAME, Long.TYPE);
            seekMethod.setAccessible(true);
        } catch (Exception ex) {
            LogHelper.printException(VideoInformation.class, "Failed to initialize", ex);
        }
    }

    /**
     * Id of the current video playing.  Includes Shorts and YouTube Stories.
     *
     * @return The id of the video. Empty string if not set yet.
     */
    @NonNull
    public static String getVideoId() {
        return videoId;
    }

    /**
     * Injection point.
     *
     * @param newlyLoadedVideoId id of the current video
     */
    public static void setVideoId(@NonNull String newlyLoadedVideoId) {
        if (!videoId.equals(newlyLoadedVideoId)) {
            LogHelper.printDebug(VideoInformation.class, "New video id: " + newlyLoadedVideoId);
            videoId = newlyLoadedVideoId;
        }
    }

    /**
     * Seek on the current video.
     * Does not function for playback of Shorts.
     * <p>
     * Caution: If called from a videoTimeHook() callback,
     * this will cause a recursive call into the same videoTimeHook() callback.
     *
     * @param millisecond The millisecond to seek the video to.
     * @return if the seek was successful
     */
    public static boolean seekTo(final long millisecond) {
        ReVancedUtils.verifyOnMainThread();
        try {
            LogHelper.printDebug(VideoInformation.class, "Seeking to " + millisecond);
            return (Boolean) seekMethod.invoke(playerControllerRef.get(), millisecond);
        } catch (Exception ex) {
            LogHelper.printException(VideoInformation.class, "Failed to seek", ex);
            return false;
        }
    }

    public static boolean seekToRelative(long millisecondsRelative) {
        return seekTo(videoTime + millisecondsRelative);
    }

    /**
     * Length of the current video playing.  Includes Shorts.
     *
     * @return The length of the video in milliseconds.
     * If the video is not yet loaded, or if the video is playing in the background with no video visible,
     * then this returns zero.
     */
    public static long getVideoLength() {
        return videoLength;
    }

    /**
     * Injection point.
     *
     * @param length The length of the video in milliseconds.
     */
    public static void setVideoLength(final long length) {
        if (videoLength != length) {
            //LogHelper.printDebug(VideoInformation.class, "Current video length: " + length);
            videoLength = length;
        }
    }

    /**
     * Playback time of the current video playing.  Includes Shorts.
     * <p>
     * Value will lag behind the actual playback time by a variable amount based on the playback speed.
     * <p>
     * If playback speed is 2.0x, this value may be up to 2000ms behind the actual playback time.
     * If playback speed is 1.0x, this value may be up to 1000ms behind the actual playback time.
     * If playback speed is 0.5x, this value may be up to 500ms behind the actual playback time.
     * Etc.
     *
     * @return The time of the video in milliseconds. -1 if not set yet.
     */
    public static long getVideoTime() {
        return videoTime;
    }

    /**
     * Injection point.
     * Called on the main thread every 1000ms.
     *
     * @param currentPlaybackTime The current playback time of the video in milliseconds.
     */
    public static void setVideoTime(final long currentPlaybackTime) {
        //LogHelper.printDebug(VideoInformation.class, "Current video time: " + currentPlaybackTime);
        videoTime = currentPlaybackTime;
    }

}
