package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.ExpressionState
import com.example.ui.theme.DynaPalette
import kotlinx.coroutines.delay

@Composable
fun DynamicIslandFace(
  expression: ExpressionState,
  featureColor: Color = DynaPalette.IslandTextPrimary,
  accentColor: Color = DynaPalette.IslandTextPrimary,
  animationEnabled: Boolean = true,
  sizeDp: Dp = 24.dp,
  modifier: Modifier = Modifier,
) {
  var isBlinking by remember { mutableStateOf(false) }

  LaunchedEffect(expression, animationEnabled) {
    if (!animationEnabled) {
      isBlinking = false
      return@LaunchedEffect
    }
    while (true) {
      delay(3_600L)
      isBlinking = true
      delay(130L)
      isBlinking = false
    }
  }

  val infinite = rememberInfiniteTransition(label = "face_idle_micro")
  val breathScale by
    infinite.animateFloat(
      initialValue = 1f,
      targetValue = if (animationEnabled) 1.045f else 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "breath",
    )
  val gazeOffsetFactor by
    infinite.animateFloat(
      initialValue = -0.03f,
      targetValue = if (animationEnabled) 0.03f else 0f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 4_200, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "gaze",
    )

  val springSpec =
    if (animationEnabled) {
      spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
      )
    } else {
      snap()
    }

  val blinkScale by
    animateFloatAsState(
      targetValue = if (isBlinking) 0.1f else 1f,
      animationSpec = tween(durationMillis = if (animationEnabled) 90 else 0),
      label = "blink",
    )

  val mouthCurveTarget =
    when (expression) {
      ExpressionState.SMILE,
      ExpressionState.HAPPY,
      ExpressionState.HEART_EYES,
      ExpressionState.XD_LAUGH,
      ExpressionState.SUNGLASSES,
      ExpressionState.TONGUE_OUT,
      ExpressionState.CHEERFUL,
      ExpressionState.WINK,
      ExpressionState.DEVILISH,
      ExpressionState.SLY -> 1f
      ExpressionState.SAD,
      ExpressionState.WORRIED,
      ExpressionState.ANGRY,
      ExpressionState.FROWN,
      ExpressionState.KO_X -> -1f
      else -> 0f
    }
  val mouthCurve by
    animateFloatAsState(targetValue = mouthCurveTarget, animationSpec = springSpec, label = "mouth")

  val browTiltTarget =
    when (expression) {
      ExpressionState.ANGRY,
      ExpressionState.DEVILISH,
      ExpressionState.SLY -> 1f
      ExpressionState.WORRIED,
      ExpressionState.SAD -> -0.7f
      ExpressionState.CHEERFUL,
      ExpressionState.WINK -> -0.35f
      else -> 0f
    }
  val browTilt by
    animateFloatAsState(targetValue = browTiltTarget, animationSpec = springSpec, label = "brow")

  val exprPop by
    animateFloatAsState(
      targetValue = if (expression == ExpressionState.WAVE_ALERT) 1.08f else 1f,
      animationSpec = springSpec,
      label = "pop",
    )

  Canvas(modifier = modifier.size(sizeDp)) {
    val w = size.width
    val h = size.height
    val strokeW = (w * 0.085f).coerceAtLeast(1.4f)
    val scale = breathScale * exprPop

    withTransform({
      scale(scaleX = scale, scaleY = scale, pivot = Offset(w / 2f, h / 2f))
    }) {
      val eyeShiftX =
        if (expression == ExpressionState.MINIMAL_PEEK || expression == ExpressionState.SMILE || expression == ExpressionState.NEUTRAL) {
          w * gazeOffsetFactor
        } else {
          0f
        }

      drawBrows(expression, w, h, strokeW, browTilt, featureColor)
      drawEyes(expression, w, h, strokeW, blinkScale, eyeShiftX, featureColor, accentColor)
      drawMouthAndAccents(
        expression = expression,
        w = w,
        h = h,
        strokeW = strokeW,
        mouthCurve = mouthCurve,
        featureColor = featureColor,
        accentColor = accentColor,
      )
    }
  }
}

private fun DrawScope.drawBrows(
  expression: ExpressionState,
  w: Float,
  h: Float,
  strokeW: Float,
  browTilt: Float,
  color: Color,
) {
  val browY = h * 0.16f
  val leftCenter = w * 0.30f
  val rightCenter = w * 0.70f
  val halfLen = w * 0.10f

  when (expression) {
    ExpressionState.WORRIED,
    ExpressionState.CHEERFUL -> {
      val leftArc =
        Path().apply {
          moveTo(leftCenter - halfLen, browY + h * 0.04f)
          quadraticTo(leftCenter, browY - h * 0.06f, leftCenter + halfLen, browY + h * 0.02f)
        }
      val rightArc =
        Path().apply {
          moveTo(rightCenter - halfLen, browY + h * 0.02f)
          quadraticTo(rightCenter, browY - h * 0.06f, rightCenter + halfLen, browY + h * 0.04f)
        }
      drawPath(leftArc, color, style = Stroke(strokeW * 0.85f, cap = StrokeCap.Round))
      drawPath(rightArc, color, style = Stroke(strokeW * 0.85f, cap = StrokeCap.Round))
    }
    ExpressionState.ANGRY,
    ExpressionState.DEVILISH -> {
      val dy = h * 0.065f * browTilt
      drawLine(
        color = color,
        start = Offset(leftCenter - halfLen, browY - dy * 0.5f),
        end = Offset(leftCenter + halfLen, browY + dy),
        strokeWidth = strokeW * 0.9f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = color,
        start = Offset(rightCenter - halfLen, browY + dy),
        end = Offset(rightCenter + halfLen, browY - dy * 0.5f),
        strokeWidth = strokeW * 0.9f,
        cap = StrokeCap.Round,
      )
      if (expression == ExpressionState.DEVILISH) {
        val leftHorn =
          Path().apply {
            moveTo(w * 0.18f, h * 0.10f)
            lineTo(w * 0.25f, -h * 0.03f)
            lineTo(w * 0.32f, h * 0.10f)
            close()
          }
        val rightHorn =
          Path().apply {
            moveTo(w * 0.68f, h * 0.10f)
            lineTo(w * 0.75f, -h * 0.03f)
            lineTo(w * 0.82f, h * 0.10f)
            close()
          }
        drawPath(leftHorn, color)
        drawPath(rightHorn, color)
      }
    }
    ExpressionState.SMIRK -> {
      drawLine(
        color = color,
        start = Offset(leftCenter - halfLen, browY - h * 0.03f),
        end = Offset(leftCenter + halfLen, browY - h * 0.03f),
        strokeWidth = strokeW * 0.85f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = color,
        start = Offset(rightCenter - halfLen, browY + h * 0.03f),
        end = Offset(rightCenter + halfLen, browY + h * 0.03f),
        strokeWidth = strokeW * 0.85f,
        cap = StrokeCap.Round,
      )
    }
    ExpressionState.WINK -> {
      val leftArc =
        Path().apply {
          moveTo(leftCenter - halfLen, browY + h * 0.02f)
          quadraticTo(leftCenter, browY - h * 0.05f, leftCenter + halfLen, browY + h * 0.02f)
        }
      drawPath(leftArc, color, style = Stroke(strokeW * 0.85f, cap = StrokeCap.Round))
    }
    else -> Unit
  }
}

private fun DrawScope.drawEyes(
  expression: ExpressionState,
  w: Float,
  h: Float,
  strokeW: Float,
  blinkScale: Float,
  eyeShiftX: Float,
  color: Color,
  accentColor: Color,
) {
  val eyeY = if (expression == ExpressionState.MINIMAL_PEEK) h * 0.30f else h * 0.34f
  val leftX = w * 0.31f + eyeShiftX
  val rightX = w * 0.69f + eyeShiftX
  val eyeSize = w * 0.155f

  when (expression) {
    ExpressionState.HEART_EYES -> {
      drawHeart(Offset(leftX, eyeY), eyeSize * 1.18f, color)
      drawHeart(Offset(rightX, eyeY), eyeSize * 1.18f, color)
    }
    ExpressionState.SUNGLASSES -> {
      val frameY = h * 0.25f
      val lensW = w * 0.30f
      val lensH = h * 0.22f
      drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.14f, frameY),
        size = Size(lensW, lensH),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
      )
      drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.56f, frameY),
        size = Size(lensW, lensH),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
      )
      drawLine(
        color = color,
        start = Offset(w * 0.11f, frameY + lensH * 0.24f),
        end = Offset(w * 0.89f, frameY + lensH * 0.24f),
        strokeWidth = strokeW * 0.9f,
        cap = StrokeCap.Round,
      )
    }
    ExpressionState.XD_LAUGH -> {
      val r = eyeSize * 0.55f
      val leftChevron =
        Path().apply {
          moveTo(leftX - r, eyeY - r * 0.8f)
          lineTo(leftX + r * 0.7f, eyeY)
          lineTo(leftX - r, eyeY + r * 0.8f)
        }
      val rightChevron =
        Path().apply {
          moveTo(rightX + r, eyeY - r * 0.8f)
          lineTo(rightX - r * 0.7f, eyeY)
          lineTo(rightX + r, eyeY + r * 0.8f)
        }
      drawPath(
        leftChevron,
        color,
        style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
      drawPath(
        rightChevron,
        color,
        style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
    }
    ExpressionState.KO_X -> {
      drawCross(Offset(leftX, eyeY), eyeSize * 0.52f, strokeW, color)
      drawCross(Offset(rightX, eyeY), eyeSize * 0.52f, strokeW, color)
    }
    ExpressionState.SLEEPY_ZZZ -> {
      drawClosedArcEye(Offset(leftX, eyeY), eyeSize * 0.65f, strokeW, color)
      drawClosedArcEye(Offset(rightX, eyeY), eyeSize * 0.65f, strokeW, color)
    }
    ExpressionState.WINK -> {
      drawRoundedEye(Offset(leftX, eyeY), eyeSize, eyeSize * blinkScale, color)
      drawLine(
        color = color,
        start = Offset(rightX - eyeSize * 0.6f, eyeY),
        end = Offset(rightX + eyeSize * 0.6f, eyeY),
        strokeWidth = strokeW,
        cap = StrokeCap.Round,
      )
    }
    ExpressionState.HAPPY -> {
      val tallH = eyeSize * 1.35f * blinkScale
      drawRoundedEye(Offset(leftX, eyeY), eyeSize * 0.92f, tallH, color)
      drawRoundedEye(Offset(rightX, eyeY), eyeSize * 0.92f, tallH, color)
    }
    ExpressionState.SLY -> {
      val s = eyeSize * 0.95f
      drawLine(
        color = color,
        start = Offset(leftX - s * 0.55f, eyeY - s * 0.35f),
        end = Offset(leftX + s * 0.55f, eyeY + s * 0.35f),
        strokeWidth = strokeW * 1.45f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = color,
        start = Offset(rightX - s * 0.55f, eyeY + s * 0.35f),
        end = Offset(rightX + s * 0.55f, eyeY - s * 0.35f),
        strokeWidth = strokeW * 1.45f,
        cap = StrokeCap.Round,
      )
    }
    ExpressionState.WAVE_ALERT -> {
      drawCircle(color = accentColor.copy(alpha = 0.32f), radius = eyeSize * 0.95f, center = Offset(leftX, eyeY))
      drawCircle(color = accentColor.copy(alpha = 0.32f), radius = eyeSize * 0.95f, center = Offset(rightX, eyeY))
      drawRoundedEye(Offset(leftX, eyeY), eyeSize * 1.05f, eyeSize * 1.05f * blinkScale, color)
      drawRoundedEye(Offset(rightX, eyeY), eyeSize * 1.05f, eyeSize * 1.05f * blinkScale, color)
    }
    else -> {
      val actualH = (eyeSize * blinkScale).coerceAtLeast(strokeW)
      drawRoundedEye(Offset(leftX, eyeY), eyeSize, actualH, color)
      drawRoundedEye(Offset(rightX, eyeY), eyeSize, actualH, color)
    }
  }
}

private fun DrawScope.drawMouthAndAccents(
  expression: ExpressionState,
  w: Float,
  h: Float,
  strokeW: Float,
  mouthCurve: Float,
  featureColor: Color,
  accentColor: Color,
) {
  val mouthY = h * 0.69f
  val mouthHalfW = w * 0.21f
  val centerX = w * 0.50f

  when (expression) {
    ExpressionState.MINIMAL_PEEK -> Unit
    ExpressionState.HAPPY,
    ExpressionState.CHEERFUL,
    ExpressionState.WINK -> {
      val grin =
        Path().apply {
          moveTo(centerX - mouthHalfW, mouthY - h * 0.04f)
          lineTo(centerX + mouthHalfW, mouthY - h * 0.04f)
          quadraticTo(centerX, mouthY + h * 0.20f, centerX - mouthHalfW, mouthY - h * 0.04f)
          close()
        }
      drawPath(grin, featureColor)
    }
    ExpressionState.DEVILISH -> {
      val grin =
        Path().apply {
          moveTo(centerX - mouthHalfW, mouthY - h * 0.04f)
          lineTo(centerX + mouthHalfW, mouthY - h * 0.04f)
          quadraticTo(centerX, mouthY + h * 0.18f, centerX - mouthHalfW, mouthY - h * 0.04f)
          close()
        }
      drawPath(grin, featureColor)
    }
    ExpressionState.SURPRISED -> {
      drawRoundRect(
        color = featureColor,
        topLeft = Offset(centerX - w * 0.11f, mouthY - h * 0.08f),
        size = Size(w * 0.22f, h * 0.20f),
        cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
      )
    }
    ExpressionState.SLEEPY_ZZZ -> {
      drawRoundRect(
        color = featureColor,
        topLeft = Offset(centerX - w * 0.06f, mouthY - h * 0.06f),
        size = Size(w * 0.12f, h * 0.17f),
        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
      )
      drawZzzAccent(w, h, strokeW, accentColor)
    }
    ExpressionState.WAVY_DIZZY -> {
      val wave =
        Path().apply {
          moveTo(centerX - mouthHalfW, mouthY)
          lineTo(centerX - mouthHalfW * 0.5f, mouthY - h * 0.05f)
          lineTo(centerX, mouthY + h * 0.05f)
          lineTo(centerX + mouthHalfW * 0.5f, mouthY - h * 0.05f)
          lineTo(centerX + mouthHalfW, mouthY)
        }
      drawPath(
        wave,
        featureColor,
        style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
    }
    ExpressionState.TONGUE_OUT -> {
      val smile =
        Path().apply {
          moveTo(centerX - mouthHalfW, mouthY - h * 0.04f)
          quadraticTo(centerX, mouthY + h * 0.14f, centerX + mouthHalfW, mouthY - h * 0.04f)
        }
      drawPath(smile, featureColor, style = Stroke(strokeW, cap = StrokeCap.Round))
      val tongue =
        Path().apply {
          moveTo(centerX - w * 0.12f, mouthY + h * 0.03f)
          quadraticTo(centerX - w * 0.15f, mouthY + h * 0.18f, centerX - w * 0.03f, mouthY + h * 0.08f)
        }
      drawPath(tongue, featureColor, style = Stroke(strokeW * 1.15f, cap = StrokeCap.Round))
    }
    ExpressionState.MUTE_X -> {
      drawCross(Offset(centerX, mouthY), w * 0.10f, strokeW, featureColor)
    }
    ExpressionState.KISS -> {
      val pucker =
        Path().apply {
          moveTo(centerX - w * 0.04f, mouthY - h * 0.06f)
          quadraticTo(centerX + w * 0.08f, mouthY - h * 0.03f, centerX - w * 0.02f, mouthY)
          quadraticTo(centerX + w * 0.08f, mouthY + h * 0.04f, centerX - w * 0.04f, mouthY + h * 0.07f)
        }
      drawPath(pucker, featureColor, style = Stroke(strokeW * 0.9f, cap = StrokeCap.Round))
    }
    ExpressionState.SOUR -> {
      val left =
        Path().apply {
          moveTo(centerX - w * 0.14f, mouthY - h * 0.05f)
          quadraticTo(centerX - w * 0.03f, mouthY, centerX - w * 0.14f, mouthY + h * 0.06f)
        }
      val right =
        Path().apply {
          moveTo(centerX + w * 0.14f, mouthY - h * 0.05f)
          quadraticTo(centerX + w * 0.03f, mouthY, centerX + w * 0.14f, mouthY + h * 0.06f)
        }
      drawPath(left, featureColor, style = Stroke(strokeW * 0.85f, cap = StrokeCap.Round))
      drawPath(right, featureColor, style = Stroke(strokeW * 0.85f, cap = StrokeCap.Round))
      drawLine(
        color = featureColor,
        start = Offset(centerX - w * 0.07f, mouthY),
        end = Offset(centerX + w * 0.07f, mouthY),
        strokeWidth = strokeW * 0.85f,
      )
    }
    ExpressionState.NEUTRAL,
    ExpressionState.SMIRK -> {
      drawLine(
        color = featureColor,
        start = Offset(centerX - mouthHalfW * 0.72f, mouthY),
        end = Offset(centerX + mouthHalfW * 0.72f, mouthY),
        strokeWidth = strokeW,
        cap = StrokeCap.Round,
      )
    }
    else -> {
      val ctrlOffset = h * 0.15f * mouthCurve
      val curve =
        Path().apply {
          moveTo(centerX - mouthHalfW, mouthY - ctrlOffset * 0.25f)
          quadraticTo(centerX, mouthY + ctrlOffset, centerX + mouthHalfW, mouthY - ctrlOffset * 0.25f)
        }
      drawPath(curve, featureColor, style = Stroke(strokeW, cap = StrokeCap.Round))
    }
  }
}

private fun DrawScope.drawRoundedEye(center: Offset, width: Float, height: Float, color: Color) {
  val radius = (width.coerceAtMost(height) * 0.28f).coerceAtLeast(1f)
  drawRoundRect(
    color = color,
    topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
    size = Size(width, height),
    cornerRadius = CornerRadius(radius, radius),
  )
}

private fun DrawScope.drawClosedArcEye(center: Offset, halfW: Float, strokeW: Float, color: Color) {
  val arc =
    Path().apply {
      moveTo(center.x - halfW, center.y - halfW * 0.2f)
      quadraticTo(center.x, center.y + halfW * 0.7f, center.x + halfW, center.y - halfW * 0.2f)
    }
  drawPath(arc, color, style = Stroke(strokeW * 0.9f, cap = StrokeCap.Round))
}

private fun DrawScope.drawCross(center: Offset, r: Float, strokeW: Float, color: Color) {
  drawLine(
    color = color,
    start = Offset(center.x - r, center.y - r),
    end = Offset(center.x + r, center.y + r),
    strokeWidth = strokeW,
    cap = StrokeCap.Round,
  )
  drawLine(
    color = color,
    start = Offset(center.x + r, center.y - r),
    end = Offset(center.x - r, center.y + r),
    strokeWidth = strokeW,
    cap = StrokeCap.Round,
  )
}

private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
  val half = size * 0.55f
  val path =
    Path().apply {
      moveTo(center.x, center.y + half * 0.85f)
      cubicTo(
        center.x - half * 1.4f,
        center.y + half * 0.1f,
        center.x - half * 1.1f,
        center.y - half * 1.0f,
        center.x,
        center.y - half * 0.35f,
      )
      cubicTo(
        center.x + half * 1.1f,
        center.y - half * 1.0f,
        center.x + half * 1.4f,
        center.y + half * 0.1f,
        center.x,
        center.y + half * 0.85f,
      )
      close()
    }
  drawPath(path, color)
}

private fun DrawScope.drawZzzAccent(w: Float, h: Float, strokeW: Float, color: Color) {
  val x0 = w * 0.68f
  val y0 = h * 0.67f
  val z = w * 0.08f
  val path =
    Path().apply {
      moveTo(x0, y0)
      lineTo(x0 + z, y0)
      lineTo(x0, y0 + z)
      lineTo(x0 + z, y0 + z)
    }
  drawPath(path, color, style = Stroke(strokeW * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
