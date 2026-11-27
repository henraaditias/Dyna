package com.example.platform.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.SettingsRepository
import com.example.ui.components.DynaIslandPill
import com.example.ui.theme.DynaTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private class OverlayLifecycleOwner :
  LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
  private val lifecycleRegistry = LifecycleRegistry(this)
  private val savedStateController = SavedStateRegistryController.create(this)
  override val viewModelStore = ViewModelStore()

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateController.savedStateRegistry

  fun onCreate() {
    savedStateController.performRestore(null)
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  fun onResume() {
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
  }

  fun onDestroy() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    viewModelStore.clear()
  }
}

class OverlayWindowController(
  private val context: Context,
  private val settingsRepository: SettingsRepository,
  private val activityRepository: ActivityRepository,
) {
  private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  private var overlayView: ComposeView? = null
  private var lifecycleOwner: OverlayLifecycleOwner? = null
  private var layoutParams: WindowManager.LayoutParams? = null

  private val _isAttached = MutableStateFlow(false)
  val isAttached: StateFlow<Boolean> = _isAttached.asStateFlow()

  fun attachIfPermitted(topOffsetDp: Float): Boolean {
    if (!Settings.canDrawOverlays(context)) {
      detach()
      return false
    }
    if (overlayView != null) {
      updateTopOffset(topOffsetDp)
      return true
    }

    val owner = OverlayLifecycleOwner().apply { onCreate() }
    val params = createLayoutParams(topOffsetDp)

    val composeView =
      ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setViewTreeLifecycleOwner(owner)
        setViewTreeViewModelStoreOwner(owner)
        setViewTreeSavedStateRegistryOwner(owner)
        setContent {
          DynaTheme {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(
              initialValue = com.example.domain.model.UserSettings()
            )
            val activity by activityRepository.currentActivity.collectAsStateWithLifecycle()
            val isExpanded by activityRepository.isOverlayExpanded.collectAsStateWithLifecycle()

            DynaIslandPill(
              activity = activity,
              appearance = settings.appearance,
              isExpanded = isExpanded,
              showNotificationPreview = settings.showNotificationPreview,
              maxWidthDp = 340f,
              onClick = { activityRepository.toggleOverlayExpanded() },
              onToggleTimerPause = { activityRepository.toggleTimerPause() },
              onStopTimer = { activityRepository.stopTimer() },
              onDismissNotification = { activityRepository.dismissActiveNotification() },
            )
          }
        }
      }

    val added =
      runCatching {
          windowManager.addView(composeView, params)
          owner.onResume()
          overlayView = composeView
          lifecycleOwner = owner
          layoutParams = params
          _isAttached.value = true
          true
        }
        .getOrElse {
          owner.onDestroy()
          _isAttached.value = false
          false
        }

    return added
  }

  fun updateTopOffset(topOffsetDp: Float) {
    val view = overlayView ?: return
    val params = layoutParams ?: return
    val nextY = dpToPx(topOffsetDp)
    if (params.y != nextY) {
      params.y = nextY
      runCatching { windowManager.updateViewLayout(view, params) }
    }
  }

  fun detach() {
    val view = overlayView
    val owner = lifecycleOwner
    overlayView = null
    lifecycleOwner = null
    layoutParams = null
    _isAttached.value = false

    if (view != null) {
      runCatching { windowManager.removeViewImmediate(view) }
    }
    owner?.onDestroy()
  }

  private fun createLayoutParams(topOffsetDp: Float): WindowManager.LayoutParams {
    val flags =
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

    return WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        flags,
        PixelFormat.TRANSLUCENT,
      )
      .apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        x = 0
        y = dpToPx(topOffsetDp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
      }
  }

  private fun dpToPx(dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).roundToInt().coerceAtLeast(0)
  }
}
