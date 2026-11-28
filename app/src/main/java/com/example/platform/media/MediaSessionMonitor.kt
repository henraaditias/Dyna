package com.example.platform.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.Rating
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.example.domain.model.MediaSnapshot
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MediaSessionMonitor(
  private val context: Context,
  private val activityRepository: ActivityRepository,
  private val settingsRepository: SettingsRepository,
  private val scope: CoroutineScope,
) {
  private val mediaSessionManager =
    context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
  private val mainHandler = Handler(Looper.getMainLooper())
  private var listenerComponent: ComponentName? = null
  private var activeController: MediaController? = null
  private var isStarted = false

  private val sessionsChangedListener =
    MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
      updateFromControllers(controllers.orEmpty())
    }

  private val controllerCallback =
    object : MediaController.Callback() {
      override fun onPlaybackStateChanged(state: PlaybackState?) {
        publishControllerState(activeController)
      }

      override fun onMetadataChanged(metadata: MediaMetadata?) {
        publishControllerState(activeController)
      }

      override fun onSessionDestroyed() {
        refreshSessions()
      }
    }

  fun start(notificationListenerComponent: ComponentName) {
    listenerComponent = notificationListenerComponent
    val manager = mediaSessionManager ?: return
    if (isStarted) {
      refreshSessions()
      return
    }
    runCatching {
      manager.addOnActiveSessionsChangedListener(
        sessionsChangedListener,
        notificationListenerComponent,
        mainHandler,
      )
      isStarted = true
      refreshSessions()
    }
  }

  fun stop() {
    val manager = mediaSessionManager
    if (isStarted && manager != null) {
      runCatching { manager.removeOnActiveSessionsChangedListener(sessionsChangedListener) }
    }
    isStarted = false
    detachCurrentController()
    activityRepository.onMediaUpdated(null)
  }

  fun refreshSessions() {
    val manager = mediaSessionManager ?: return
    val component = listenerComponent ?: return
    val controllers =
      runCatching { manager.getActiveSessions(component) }.getOrNull().orEmpty()
    updateFromControllers(controllers)
  }

  fun togglePlayPause(): Boolean {
    val controller = activeController ?: return false
    val state = controller.playbackState?.state
    return runCatching {
        if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING) {
          controller.transportControls.pause()
        } else {
          controller.transportControls.play()
        }
        true
      }
      .getOrDefault(false)
  }

  fun skipToNext(): Boolean {
    val controller = activeController ?: return false
    return runCatching {
        controller.transportControls.skipToNext()
        true
      }
      .getOrDefault(false)
  }

  fun skipToPrevious(): Boolean {
    val controller = activeController ?: return false
    return runCatching {
        controller.transportControls.skipToPrevious()
        true
      }
      .getOrDefault(false)
  }

  fun toggleFavorite(): Boolean {
    val controller = activeController ?: return false
    val playbackState = controller.playbackState
    val favoriteCustomAction = findFavoriteCustomAction(playbackState)
    if (favoriteCustomAction != null) {
      return runCatching {
          controller.transportControls.sendCustomAction(favoriteCustomAction, null)
          true
        }
        .getOrDefault(false)
    }

    val actions = playbackState?.actions ?: 0L
    if ((actions and PlaybackState.ACTION_SET_RATING) != 0L) {
      val currentHeart = controller.metadata?.getRating(MediaMetadata.METADATA_KEY_USER_RATING)?.hasHeart() == true
      return runCatching {
          controller.transportControls.setRating(Rating.newHeartRating(!currentHeart))
          true
        }
        .getOrDefault(false)
    }
    return false
  }

  private fun updateFromControllers(controllers: List<MediaController>) {
    val preferred =
      controllers.firstOrNull { controller ->
        val st = controller.playbackState?.state
        st == PlaybackState.STATE_PLAYING || st == PlaybackState.STATE_BUFFERING
      } ?: controllers.firstOrNull { controller ->
        val st = controller.playbackState?.state
        st == PlaybackState.STATE_PAUSED &&
          !controller.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank()
      }

    if (preferred?.sessionToken != activeController?.sessionToken) {
      detachCurrentController()
      activeController = preferred
      preferred?.registerCallback(controllerCallback, mainHandler)
    }

    publishControllerState(preferred)
  }

  private fun publishControllerState(controller: MediaController?) {
    scope.launch {
      val settings = settingsRepository.settings.first()
      if (!settings.mediaActivityEnabled || !settings.isOverlayEnabled || controller == null) {
        activityRepository.onMediaUpdated(null)
        return@launch
      }

      val playbackState = controller.playbackState
      val stateCode = playbackState?.state ?: PlaybackState.STATE_NONE
      if (stateCode == PlaybackState.STATE_STOPPED || stateCode == PlaybackState.STATE_NONE) {
        activityRepository.onMediaUpdated(null)
        return@launch
      }

      val metadata = controller.metadata
      val title =
        metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
          ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)?.takeIf { it.isNotBlank() }
          ?: run {
            activityRepository.onMediaUpdated(null)
            return@launch
          }

      val artist =
        metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
          ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)?.takeIf { it.isNotBlank() }
      val duration = (metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L).coerceAtLeast(0L)
      val position = (playbackState?.position ?: 0L).coerceAtLeast(0L)
      val actions = playbackState?.actions ?: 0L
      val pkg = controller.packageName
      val appName =
        runCatching {
            val info = context.packageManager.getApplicationInfo(pkg, 0)
            context.packageManager.getApplicationLabel(info).toString()
          }
          .getOrDefault(pkg)

      val rawArt =
        metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
          ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
          ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
      val albumArt =
        rawArt?.let { bmp ->
          if (bmp.width <= 128 && bmp.height <= 128) bmp
          else Bitmap.createScaledBitmap(bmp, 128, 128, true)
        }

      val favoriteCustom = findFavoriteCustomAction(playbackState)
      val ratingSupported = (actions and PlaybackState.ACTION_SET_RATING) != 0L
      val hasFavorite = favoriteCustom != null || ratingSupported
      val userRating = metadata?.getRating(MediaMetadata.METADATA_KEY_USER_RATING)
      val isFavorited =
        userRating?.hasHeart() == true ||
          favoriteCustom?.name?.toString()?.lowercase()?.let {
            it.contains("remove") || it.contains("unlike") || it.contains("saved")
          } == true

      activityRepository.onMediaUpdated(
        MediaSnapshot(
          packageName = pkg,
          appName = appName,
          title = title,
          artist = artist,
          isPlaying =
            stateCode == PlaybackState.STATE_PLAYING || stateCode == PlaybackState.STATE_BUFFERING,
          durationMs = duration,
          positionMs = position,
          hasPrevious = (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L || actions == 0L,
          hasNext = (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L || actions == 0L,
          albumArt = albumArt,
          hasFavoriteAction = hasFavorite,
          isFavorited = isFavorited,
        )
      )
    }
  }

  private fun findFavoriteCustomAction(playbackState: PlaybackState?): PlaybackState.CustomAction? =
    playbackState?.customActions?.firstOrNull { custom ->
      val combined = "${custom.action} ${custom.name}".lowercase()
      combined.contains("fav") ||
        combined.contains("like") ||
        combined.contains("heart") ||
        combined.contains("love") ||
        combined.contains("save")
    }

  private fun detachCurrentController() {
    activeController?.let { runCatching { it.unregisterCallback(controllerCallback) } }
    activeController = null
  }
}
