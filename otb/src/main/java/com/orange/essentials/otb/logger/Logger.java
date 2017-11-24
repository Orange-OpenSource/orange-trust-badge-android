package com.orange.essentials.otb.logger;

import android.util.Log;

/**
 * Custom logger that can be deactivated.
 */
public final class Logger
{
    // ************************ CONSTANTS *******************************************************************

    /** Standard appended message for log when entering a method. */
    public static final String ENTERING = "entering";

    /** Standard appended message for log when returning from a method. */
    public static final String RETURNING = "returning";

    /** The depth to go in the call stack, to get interesting calling method name. */
    private static final int DEPTH = 4;

    // ************************ FIELDS **********************************************************************

    /** Activation status. by default disabled. */
    private static boolean mIsLoggingAllowed = false;

    // ************************ METHODS *********************************************************************

    /**
     * Private constructor. This is a tool class, it should not be instantiated.
     */
    private Logger()
    {
    }

    /**
     * @return true if logging is allowed, false otherwise.
     */
    @SuppressWarnings("unused")
    public static boolean isLoggingAllowed()
    {
        return mIsLoggingAllowed;
    }

    /**
     * To allow logging.
     */
    public static void allowLogging()
    {
        mIsLoggingAllowed = true;
    }

    /**
     * Disable logging.
     */
    public static void disableLogging()
    {
        mIsLoggingAllowed = false;
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    public static int v(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.v(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    public static int v(final String tag, final String msg, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.v(tag, updatedMsg, tr);
        }

        return bytesWritten;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    public static int d(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.d(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    public static int d(final String tag, final String msg, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.d(tag, updatedMsg + '\n' + getStackTraceString(tr));
        }

        return bytesWritten;
    }

    /**
     * Send an INFO log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    public static int i(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.i(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    public static int i(final String tag, final String msg, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.i(tag, updatedMsg + '\n' + getStackTraceString(tr));
        }

        return bytesWritten;
    }

    /**
     * Send a WARN log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    public static int w(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.w(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    public static int w(final String tag, final String msg, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.w(tag, updatedMsg, tr);
        }

        return bytesWritten;
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    @SuppressWarnings("unused")
    public static int w(final String tag, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH);

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.w(tag, updatedMsg, tr);
        }

        return bytesWritten;
    }

    /**
     * Send an ERROR log message.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    public static int e(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.e(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    public static int e(final String tag, final String msg, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.e(tag, updatedMsg, tr);
        }

        return bytesWritten;
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the {@link android.os.DropBoxManager} and/or
     * the process may be terminated
     * immediately with an error dialog.
     *
     * @param tag
     *            Used to identify the source of a log message.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static int wtf(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.wtf(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Like {@link #wtf(String, String)}, but also writes to the log the full
     * call stack.
     *
     * @param tag
     *            Used to identify the source of a log message.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    @SuppressWarnings("unused")
    public static int wtfStack(final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.wtf(tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, String)}, with an exception to log.
     *
     * @param tag
     *            Used to identify the source of a log message.
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static int wtf(final String tag, final Throwable tr)
    {
        return wtf(tag, tr.getMessage(), tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, Throwable)}, with a message as well.
     *
     * @param tag
     *            Used to identify the source of a log message.
     * @param msg
     *            The message you would like logged.
     * @param tr
     *            An exception to log. May be null.
     * @return The number of bytes written.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static int wtf(final String tag, final String msg, final Throwable tr)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.wtf(tag, updatedMsg, tr);
        }

        return bytesWritten;
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable.
     *
     * @param tr
     *            An exception to log.
     * @return The number of bytes written.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static String getStackTraceString(final Throwable tr)
    {
        return Log.getStackTraceString(tr);
    }

    /**
     * Low-level logging call.
     *
     * @param priority
     *            The priority/type of this log message
     * @param tag
     *            Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg
     *            The message you would like logged.
     * @return The number of bytes written.
     */
    @SuppressWarnings("unused")
    public static int println(final int priority, final String tag, final String msg)
    {
        int bytesWritten = 0;
        String updatedMsg = getMethodName(DEPTH)  + "() : " + msg;

        if (mIsLoggingAllowed)
        {
            bytesWritten = Log.println(priority, tag, updatedMsg);
        }

        return bytesWritten;
    }

    /**
     * Get the method name for a provided depth in call stack.
     * @param parDepth depth in the call stack (0 means current method, 1 means call method, ...)
     * @return method name
     */
    private static String getMethodName(final int parDepth)
    {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[parDepth].getMethodName();
    }
}
