/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orange.essentials.otb.ui.utils

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.orange.essentials.otb.R
import com.orange.essentials.otb.logger.Logger
import java.lang.ref.WeakReference
import java.util.*

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 *
 *
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 *
 *
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 *
 *
 * MediaController will hide and
 * show the buttons according to these rules:
 *
 *  *  The "previous" and "next" buttons are hidden until setPrevNextListeners()
 * has been called
 *  *  The "previous" and "next" buttons are visible but disabled if
 * setPrevNextListeners() was called with null listeners
 *  *  The "rewind" and "fastforward" buttons are shown unless requested
 * otherwise by using the MediaController(Context, boolean) constructor
 * with the boolean set to false
 *
 */
class VideoControllerView : FrameLayout {

    internal var mFormatBuilder: StringBuilder? = null
    internal var mFormatter: Formatter? = null
    private var mPlayer: MediaPlayerControl? = null
    private var mContext: Context? = null
    private var mAnchor: ViewGroup? = null
    private var mRoot: View? = null
    private var mProgress: ProgressBar? = null
    private var mEndTime: TextView? = null
    private var mCurrentTime: TextView? = null
    var isShowing: Boolean = false
        private set
    private var mDragging: Boolean = false
    private var mUseFastForward: Boolean = false
    private var mFromXml: Boolean = false
    private var mListenersSet: Boolean = false
    private var mNextListener: View.OnClickListener? = null
    private var mPrevListener: View.OnClickListener? = null
    private var mPauseButton: ImageButton? = null
    private var mFfwdButton: ImageButton? = null
    private var mRewButton: ImageButton? = null
    private var mNextButton: ImageButton? = null
    private var mPrevButton: ImageButton? = null
    //    private ImageButton mFullscreenButton;
    private val mHandler = MessageHandler(this)
    private val mPauseListener = View.OnClickListener {
        doPauseResume()
        show(sDefaultTimeout)
    }
    /*private val mFullscreenListener = View.OnClickListener {
        doToggleFullscreen()
        show(sDefaultTimeout)
    }*/
    // There are two scenarios that can trigger the seek bar listener to trigger:
    //
    // The first is the user using the touch pad to adjust the position of the
    // seek bar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private val mSeekListener = object : OnSeekBarChangeListener {
        override fun onStartTrackingTouch(bar: SeekBar) {
            show(3600000)

            mDragging = true
            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS)
        }

        override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
            if (mPlayer == null) {
                return
            }

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return
            }
            val duration = mPlayer!!.duration.toLong()
            val newPosition = duration * progress / 1000L
            mPlayer!!.seekTo(newPosition.toInt())
            if (mCurrentTime != null)
                mCurrentTime!!.text = stringForTime(newPosition.toInt())
        }

        override fun onStopTrackingTouch(bar: SeekBar) {
            mDragging = false
            setProgress()
            updatePausePlay()
            show(sDefaultTimeout)
            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS)
        }
    }
    private val mRewindListener = View.OnClickListener {
        if (mPlayer == null) {
            return@OnClickListener
        }
        var pos = mPlayer!!.currentPosition
        pos -= 5000 // milliseconds
        mPlayer!!.seekTo(pos)
        setProgress()

        show(sDefaultTimeout)
    }
    private val mFastForwardListener = View.OnClickListener {
        if (mPlayer == null) {
            return@OnClickListener
        }
        var pos = mPlayer!!.currentPosition
        pos += 15000 // milliseconds
        mPlayer!!.seekTo(pos)
        setProgress()

        show(sDefaultTimeout)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mRoot = null
        mContext = context
        mUseFastForward = true
        mFromXml = true

        Logger.i(TAG, TAG)
    }

    constructor(context: Context, useFastForward: Boolean) : super(context) {
        mContext = context
        mUseFastForward = useFastForward

        Logger.i(TAG, TAG)
    }

    constructor(context: Context) : this(context, true) {
        Logger.i(TAG, TAG)
    }

    public override fun onFinishInflate() {
        if (mRoot != null) {
            initControllerView(mRoot as View)
        }
        super.onFinishInflate()
    }

    fun setMediaPlayer(player: MediaPlayerControl) {
        mPlayer = player
        updatePausePlay()
        // updateFullScreen();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.

     * @param view The view to which to anchor the controller when it is visible.
     */
    fun setAnchorView(view: ViewGroup) {
        mAnchor = view
        val frameParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        removeAllViews()
        val v = makeControllerView()
        addView(v, frameParams)
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.

     * @return The controller view.
     * *
     * @hide This doesn't work as advertised
     */
    private fun makeControllerView(): View {
        val inflate = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mRoot = inflate.inflate(R.layout.otb_mediacontroller, null)

        initControllerView(mRoot as View)

        return mRoot as View
    }

    private fun initControllerView(v: View) {
        mPauseButton = v.findViewById(R.id.pause)
        if (mPauseButton != null) {
            mPauseButton!!.requestFocus()
            mPauseButton!!.setOnClickListener(mPauseListener)
        }
        //        mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        //        if (mFullscreenButton != null) {
        //            mFullscreenButton.requestFocus();
        //            mFullscreenButton.setOnClickListener(mFullscreenListener);
        //        }
        mFfwdButton = v.findViewById(R.id.ffwd)
        if (mFfwdButton != null) {
            mFfwdButton!!.setOnClickListener(mFastForwardListener)
            if (!mFromXml) {
                mFfwdButton!!.visibility = if (mUseFastForward) View.VISIBLE else View.GONE
            }
        }

        mRewButton = v.findViewById(R.id.rew)
        if (mRewButton != null) {
            mRewButton!!.setOnClickListener(mRewindListener)
            if (!mFromXml) {
                mRewButton!!.visibility = if (mUseFastForward) View.VISIBLE else View.GONE
            }
        }
        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextButton = v.findViewById(R.id.next)
        if (mNextButton != null && !mFromXml && !mListenersSet) {
            mNextButton!!.visibility = View.GONE
        }
        mPrevButton = v.findViewById(R.id.prev)
        if (mPrevButton != null && !mFromXml && !mListenersSet) {
            mPrevButton!!.visibility = View.GONE
        }

        mProgress = v.findViewById<SeekBar>(R.id.mediacontroller_progress)
        if (mProgress != null) {
            if (mProgress is SeekBar) {
                val seeker = mProgress as SeekBar?
                seeker!!.setOnSeekBarChangeListener(mSeekListener)
            }
            mProgress!!.max = 1000
        }

        mEndTime = v.findViewById(R.id.time)
        mCurrentTime = v.findViewById(R.id.time_current)
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())

        installPrevNextListeners()
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private fun disableUnsupportedButtons() {
        if (mPlayer == null) {
            return
        }

        try {
            if (mPauseButton != null && !mPlayer!!.canPause()) {
                mPauseButton!!.isEnabled = false
            }
            if (mRewButton != null && !mPlayer!!.canSeekBackward()) {
                mRewButton!!.isEnabled = false
            }
            if (mFfwdButton != null && !mPlayer!!.canSeekForward()) {
                mFfwdButton!!.isEnabled = false
            }
        } catch (ex: IncompatibleClassChangeError) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }

    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.

     * @param timeout The timeout in milliseconds. Use 0 to show
     * *                the controller until hide() is called.
     */
    @JvmOverloads
    fun show(timeout: Int = sDefaultTimeout) {
        if (!isShowing && mAnchor != null) {
            setProgress()
            if (mPauseButton != null) {
                mPauseButton!!.requestFocus()
            }
            disableUnsupportedButtons()
            val tlp = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            )

            mAnchor!!.addView(this, tlp)
            isShowing = true
        }
        updatePausePlay()
        //        updateFullScreen();
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS)
        val msg = mHandler.obtainMessage(FADE_OUT)
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT)
            mHandler.sendMessageDelayed(msg, timeout.toLong())
        }
    }

    /**
     * Remove the controller from the screen.
     */
    fun hide() {
        if (mAnchor == null) {
            return
        }

        try {
            mAnchor!!.removeView(this)
            mHandler.removeMessages(SHOW_PROGRESS)
        } catch (ex: IllegalArgumentException) {
            Logger.w("MediaController", "already removed")
        }

        isShowing = false
    }

    private fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600

        mFormatBuilder?.setLength(0)
        if (hours > 0) {
            return mFormatter?.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            return mFormatter?.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun setProgress(): Int {
        if (mPlayer == null || mDragging) {
            return 0
        }
        val position = mPlayer!!.currentPosition
        val duration = mPlayer!!.duration
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                val pos = 1000L * position / duration
                mProgress!!.progress = pos.toInt()
            }
            val percent = mPlayer!!.bufferPercentage
            mProgress!!.secondaryProgress = percent * 10
        }

        if (mEndTime != null)
            mEndTime!!.text = stringForTime(duration)
        if (mCurrentTime != null)
            mCurrentTime!!.text = stringForTime(position)

        return position
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        show(sDefaultTimeout)
        return true
    }

    //    public void updateFullScreen() {
    //        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
    //            return;
    //        }
    //
    //        if (mPlayer.isFullScreen()) {
    //            mFullscreenButton.setImageResource(R.drawable.otb_ic_media_fullscreen_shrink);
    //        } else {
    //            mFullscreenButton.setImageResource(R.drawable.otb_ic_media_fullscreen_stretch);
    //        }
    //    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        show(sDefaultTimeout)
        return false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (mPlayer == null) {
            return true
        }
        val keyCode = event.keyCode
        val uniqueDown = event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume()
                show(sDefaultTimeout)
                if (mPauseButton != null) {
                    mPauseButton!!.requestFocus()
                }
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer!!.isPlaying) {
                mPlayer!!.start()
                updatePausePlay()
                show(sDefaultTimeout)
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer!!.isPlaying) {
                mPlayer!!.pause()
                updatePausePlay()
                show(sDefaultTimeout)
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event)
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide()
            }
            return true
        }

        show(sDefaultTimeout)
        return super.dispatchKeyEvent(event)
    }

    fun updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return
        }

        if (mPlayer!!.isPlaying) {
            mPauseButton!!.setImageResource(R.drawable.otb_ic_media_pause)
        } else {
            mPauseButton!!.setImageResource(R.drawable.otb_ic_media_play)
        }
    }

    private fun doPauseResume() {
        if (mPlayer == null) {
            return
        }

        if (mPlayer!!.isPlaying) {
            mPlayer!!.pause()
        } else {
            mPlayer!!.start()
        }
        updatePausePlay()
    }

    /*private fun doToggleFullscreen() {
        if (mPlayer == null) {
            return
        }

        mPlayer!!.toggleFullScreen()
    }*/

    override fun setEnabled(enabled: Boolean) {
        if (mPauseButton != null) {
            mPauseButton!!.isEnabled = enabled
        }
        if (mFfwdButton != null) {
            mFfwdButton!!.isEnabled = enabled
        }
        if (mRewButton != null) {
            mRewButton!!.isEnabled = enabled
        }
        if (mNextButton != null) {
            mNextButton!!.isEnabled = enabled && mNextListener != null
        }
        if (mPrevButton != null) {
            mPrevButton!!.isEnabled = enabled && mPrevListener != null
        }
        if (mProgress != null) {
            mProgress!!.isEnabled = enabled
        }
        disableUnsupportedButtons()
        super.setEnabled(enabled)
    }

    private fun installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton!!.setOnClickListener(mNextListener)
            mNextButton!!.isEnabled = mNextListener != null
        }

        if (mPrevButton != null) {
            mPrevButton!!.setOnClickListener(mPrevListener)
            mPrevButton!!.isEnabled = mPrevListener != null
        }
    }

    fun setPrevNextListeners(next: View.OnClickListener, prev: View.OnClickListener) {
        mNextListener = next
        mPrevListener = prev
        mListenersSet = true

        if (mRoot != null) {
            installPrevNextListeners()

            if (mNextButton != null && !mFromXml) {
                mNextButton!!.visibility = View.VISIBLE
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton!!.visibility = View.VISIBLE
            }
        }
    }

    interface MediaPlayerControl {
        fun start()
        fun pause()
        val duration: Int
        val currentPosition: Int
        fun seekTo(pos: Int)
        val isPlaying: Boolean
        val bufferPercentage: Int
        fun canPause(): Boolean
        fun canSeekBackward(): Boolean
        fun canSeekForward(): Boolean
        val isFullScreen: Boolean
        fun toggleFullScreen()
    }

    private class MessageHandler internal constructor(view: VideoControllerView) : Handler() {
        private val mView: WeakReference<VideoControllerView>

        init {
            mView = WeakReference(view)
        }

        override fun handleMessage(mesg: Message) {
            var msg = mesg
            val view = mView.get()
            if (view == null || view.mPlayer == null) {
                return
            }
            val pos: Int
            when (msg.what) {
                FADE_OUT -> view.hide()
                SHOW_PROGRESS -> {
                    pos = view.setProgress()
                    if (!view.mDragging && view.isShowing && view.mPlayer!!.isPlaying) {
                        msg = obtainMessage(SHOW_PROGRESS)
                        sendMessageDelayed(msg, (1000 - pos % 1000).toLong())
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "VideoControllerView"
        private const val sDefaultTimeout = 3000
        private const val FADE_OUT = 1
        private const val SHOW_PROGRESS = 2
    }
}
/**
 * Show the controller on screen. It will go away
 * automatically after 3 seconds of inactivity.
 */