package mygame.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import mygame.ClientMain;
import static mygame.helpers.StopWatch.getTimeLeft;

/**
 * TODO: PAUSE DOES NOT WORK PROPERLY.
 * @author Bjorn van der Laan (bjovan-5)
 */
public class StopWatch {

    private static long start_time = 0;
    private static final long time_limit = (long) ClientMain.START_TIME * 1000;
    private static boolean started = false;
    private static boolean paused = false;

    private StopWatch() {
    }

    public static void startTime() {
        started = true;
        paused = false;
        start_time = System.currentTimeMillis();
    }
    
    public static void pauseTime() {
        paused = true;
    }

    public static void stopTime() {
        paused = false;
        started = false;
        start_time = 0;
    }

    public static long getTimeLeft() {
        if (started) {
            return time_limit - (System.currentTimeMillis() - start_time);
        } else {
            return time_limit;
        }
    }

    /**
     * Sets the amount of time left
     *
     * @param time_left amount of time left
     */
    public static void setTimeLeft(long time_left) {
        if (started) {
            long time_expired = time_limit - time_left;
            start_time = System.currentTimeMillis() - time_expired;
        } else {
            startTime();
            setTimeLeft(time_left);
        }
    }

    /**
     * Formats time left to the right format.
     *
     * @return formatted string of the time left
     */
    public static String timeLeftToString() {
        return timeToString(getTimeLeft());
    }

    /**
     * Formats time to the right format.
     *
     * @return formatted string of the time
     */
    public static String timeToString(long input_time) {
        Date time = new Date(input_time);

        SimpleDateFormat secs_format = new SimpleDateFormat("ss");
        String secs = secs_format.format(time);
        SimpleDateFormat milli_format = new SimpleDateFormat("SSS");
        String millisecs = milli_format.format(time).substring(0, 2);

        return secs + ":" + millisecs;
    }

    /**
     * Formats time to the right format.
     *
     * @return formatted string of the time
     */
    public static String timeToString(float input_time) {
        Date time = new Date((long) input_time);

        SimpleDateFormat secs_format = new SimpleDateFormat("ss");
        String secs = secs_format.format(time);
        SimpleDateFormat milli_format = new SimpleDateFormat("SSS");
        String millisecs = milli_format.format(time).substring(0, 2);

        return secs + ":" + millisecs;
    }

    public static boolean isTimeUp() {
        return getTimeLeft() < 0;
    }
}
