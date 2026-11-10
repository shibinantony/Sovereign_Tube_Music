package com.jsab.ytmusic.bridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Bi-directional JavaScript interface bridging YouTube Music Web DOM with Android Native OS.
 * Exposes methods callable by inject.js and dispatches playback commands to WebView.
 */
class WebInterfaceBridge(webView: WebView) {

    private val webViewRef = WeakReference(webView)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(PlaybackStateData())
    val playbackState: StateFlow<PlaybackStateData> = _playbackState.asStateFlow()

    /**
     * Invoked from JavaScript (inject.js) whenever metadata, play/pause, or timeline updates.
     */
    @JavascriptInterface
    fun onPlaybackStateChanged(
        isPlaying: Boolean,
        title: String,
        artist: String,
        album: String,
        artUrl: String,
        duration: Long,
        position: Long
    ) {
        _playbackState.value = PlaybackStateData(
            isPlaying = isPlaying,
            title = title,
            artist = artist,
            album = album,
            artUrl = artUrl,
            durationSeconds = duration,
            positionSeconds = position
        )
    }

    // -------------------------------------------------------------
    // Native-to-JavaScript Dispatch Methods
    // -------------------------------------------------------------

    fun play() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.play();")
    }

    fun pause() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.pause();")
    }

    fun togglePlay() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.togglePlay();")
    }

    fun next() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.next();")
    }

    fun previous() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.previous();")
    }

    fun seekTo(seconds: Long) {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.seekTo($seconds);")
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        evaluateScript("window.jsabPlayer && window.jsabPlayer.setVolume($clamped);")
    }

    fun setEqualizer(bands: FloatArray, bassBoost: Float, preampGain: Float) {
        val bandsJson = bands.joinToString(prefix = "[", postfix = "]") { it.toString() }
        evaluateScript("window.jsabPlayer && window.jsabPlayer.setEqualizer($bandsJson, $bassBoost, $preampGain);")
    }

    fun toggleShuffle() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.toggleShuffle();")
    }

    fun toggleRepeat() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.toggleRepeat();")
    }

    fun startRadio() {
        evaluateScript("window.jsabPlayer && window.jsabPlayer.startRadio();")
    }

    fun injectShieldScript(scriptContent: String) {
        evaluateScript(scriptContent)
    }

    private fun evaluateScript(script: String) {
        mainHandler.post {
            webViewRef.get()?.evaluateJavascript(script, null)
        }
    }
}
