package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DiceDarkRed
import com.example.ui.theme.DiceIvory
import com.example.ui.theme.DiceIvoryDark
import com.example.ui.theme.DicePipDark
import com.example.ui.theme.DiceRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        ShakeRollApp()
      }
    }
  }
}

/**
 * Shake detector that calculates acceleration magnitude sqrt(x^2 + y^2 + z^2)
 * and triggers when exceeding the threshold. Includes debounce cooldown logic.
 */
class ShakeDetector(
  private val threshold: Float = 12.5f,
  private val cooldownMs: Long = 650L,
  private val onShake: () -> Unit,
) : SensorEventListener {

  private var lastShakeTimestamp: Long = 0L

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

    val x = event.values[0]
    val y = event.values[1]
    val z = event.values[2]

    // Magnitude of acceleration vector
    val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

    val currentTime = System.currentTimeMillis()
    if (magnitude >= threshold) {
      if (currentTime - lastShakeTimestamp >= cooldownMs) {
        lastShakeTimestamp = currentTime
        onShake()
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // No-op
  }
}

/**
 * Helper to trigger modern haptic feedback.
 */
fun triggerHapticThump(context: Context) {
  try {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager =
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator != null && vibrator.hasVibrator()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(65L, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(65L)
      }
    }
  } catch (_: Exception) {
    // Graceful fallback if device lacks vibrator
  }
}

fun triggerHapticTick(context: Context) {
  try {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager =
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator != null && vibrator.hasVibrator()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(18L, 90)
        vibrator.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(18L)
      }
    }
  } catch (_: Exception) {
    // Ignore
  }
}

@Composable
fun ShakeRollApp() {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()
  val isPreview = LocalInspectionMode.current

  // State
  var currentDieValue by remember { mutableIntStateOf(1) }
  var displayedDieValue by remember { mutableIntStateOf(1) }
  var isRolling by remember { mutableStateOf(false) }
  var rollCount by remember { mutableIntStateOf(0) }
  val rollHistory = remember { mutableStateListOf<Int>() }

  // Animation values
  val rotationAnimZ = remember { Animatable(0f) }
  val rotationAnimX = remember { Animatable(0f) }
  val rotationAnimY = remember { Animatable(0f) }
  val scaleAnim = remember { Animatable(1f) }

  // Function to execute roll animation and landing
  val performRoll: () -> Unit = {
    if (!isRolling) {
      isRolling = true
      coroutineScope.launch {
        // Launch rapid number shuffling and micro ticks
        val rollJob = launch {
          val shuffleCount = 10
          for (i in 0 until shuffleCount) {
            displayedDieValue = Random.nextInt(1, 7)
            triggerHapticTick(context)
            delay(50L + (i * 3L)) // slight deceleration
          }
        }

        // Animate 3D tumble and rotation
        val targetRotZ = rotationAnimZ.value + (360f * (if (Random.nextBoolean()) 1 else -1))
        val targetRotX = rotationAnimX.value + 180f
        val targetRotY = rotationAnimY.value + 180f

        val animZJob = launch {
          rotationAnimZ.animateTo(
            targetValue = targetRotZ,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
          )
        }
        val animXJob = launch {
          rotationAnimX.animateTo(
            targetValue = targetRotX,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
          )
        }
        val animYJob = launch {
          rotationAnimY.animateTo(
            targetValue = targetRotY,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
          )
        }
        val scaleJob = launch {
          scaleAnim.animateTo(1.18f, tween(180, easing = FastOutSlowInEasing))
          scaleAnim.animateTo(0.92f, tween(160, easing = FastOutSlowInEasing))
          scaleAnim.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 450f))
        }

        rollJob.join()
        animZJob.join()
        animXJob.join()
        animYJob.join()
        scaleJob.join()

        // Settle on final result
        val finalRoll = Random.nextInt(1, 7)
        currentDieValue = finalRoll
        displayedDieValue = finalRoll
        rollCount++
        if (rollHistory.size >= 12) {
          rollHistory.removeAt(0)
        }
        rollHistory.add(finalRoll)

        isRolling = false

        // Haptic thump on landing
        triggerHapticThump(context)
      }
    }
  }

  // Sensor Lifecycle Management via DisposableEffect
  if (!isPreview) {
    DisposableEffect(lifecycleOwner) {
      val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
      val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

      val shakeDetector = ShakeDetector(
        threshold = 13.0f,
        cooldownMs = 600L,
        onShake = {
          performRoll()
        },
      )

      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          accelerometer?.let {
            sensorManager.registerListener(
              shakeDetector,
              it,
              SensorManager.SENSOR_DELAY_UI,
            )
          }
        } else if (event == Lifecycle.Event.ON_PAUSE) {
          sensorManager?.unregisterListener(shakeDetector)
        }
      }

      lifecycleOwner.lifecycle.addObserver(observer)

      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
        sensorManager?.unregisterListener(shakeDetector)
      }
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    containerColor = MaterialTheme.colorScheme.background,
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {

      // Top Bar: Header, Stats and Reset
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  Brush.linearGradient(
                    listOf(DiceRed, DiceDarkRed),
                  ),
                ),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
              )
            }
            Column {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
              )
              Text(
                text = "Physical Shake Die",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          // Reset Stats Button
          if (rollCount > 0) {
            IconButton(
              onClick = {
                rollCount = 0
                rollHistory.clear()
              },
              modifier = Modifier.testTag("reset_stats_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset rolls",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Roll Badges
        if (rollHistory.isNotEmpty()) {
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
          ) {
            itemsIndexed(rollHistory.reversed()) { index, roll ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (index == 0) DiceRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                border = if (index == 0) Stroke(2f).let {
                  androidx.compose.foundation.BorderStroke(1.5.dp, DiceRed)
                } else null,
                modifier = Modifier.animateItem(),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = getDiceUnicode(roll),
                    fontSize = 18.sp,
                  )
                  Text(
                    text = roll.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                    ),
                    color = if (index == 0) DiceRed else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        } else {
          Text(
            text = stringResource(R.string.total_rolls, rollCount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          )
        }
      }

      // Middle Area: Large Rounded Die Component
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f),
      ) {
        // Die Result Callout
        Box(
          modifier = Modifier
            .height(44.dp)
            .padding(bottom = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          if (isRolling) {
            Text(
              text = stringResource(R.string.rolling),
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
              ),
              color = AccentAmber,
            )
          } else if (rollCount > 0) {
            Text(
              text = stringResource(R.string.rolled_a, currentDieValue),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
              ),
              color = MaterialTheme.colorScheme.onBackground,
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Die with 3D Transforms & Programmatic Dots Canvas
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(240.dp)
            .graphicsLayer {
              rotationZ = rotationAnimZ.value
              rotationX = rotationAnimX.value
              rotationY = rotationAnimY.value
              scaleX = scaleAnim.value
              scaleY = scaleAnim.value
              cameraDistance = 14f * density
            }
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = { performRoll() },
            )
            .semantics {
              contentDescription = "Die showing $displayedDieValue. Tap or shake to roll."
            }
            .testTag("die_square"),
        ) {
          DieFaceCanvas(
            dieValue = displayedDieValue,
            isRolling = isRolling,
            modifier = Modifier.size(210.dp),
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tap fallback pill indicator
        OutlinedCard(
          onClick = { performRoll() },
          shape = RoundedCornerShape(50),
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
          ),
          modifier = Modifier.testTag("tap_roll_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.TouchApp,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = stringResource(R.string.tap_to_roll),
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Bottom Area: Shake instruction & Sensor Status Banner
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Shake Instruction Banner
        ShakeInstructionCard(isRolling = isRolling)

        // Manual Roll Action Button for immediate convenience
        FilledTonalButton(
          onClick = { performRoll() },
          enabled = !isRolling,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("roll_action_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Casino,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isRolling) stringResource(R.string.rolling) else "Roll Dice",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
        }
      }
    }
  }
}

/**
 * Programmatic Canvas drawing for 6-sided die with realistic rounded corners,
 * smooth depth shading, and crisp pips for values 1 to 6.
 */
@Composable
fun DieFaceCanvas(
  dieValue: Int,
  isRolling: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val canvasSize = size.width
    val cornerRadius = canvasSize * 0.18f

    // 1. Soft Cast Shadow beneath die
    drawRoundRect(
      color = Color(0x28000000),
      topLeft = Offset(0f, canvasSize * 0.04f),
      size = Size(canvasSize, canvasSize),
      cornerRadius = CornerRadius(cornerRadius, cornerRadius),
      style = Fill,
    )

    // 2. Die Body gradient (Ivory / warm white depth)
    val bodyBrush = Brush.linearGradient(
      colors = listOf(DiceIvory, DiceIvoryDark),
      start = Offset(0f, 0f),
      end = Offset(canvasSize, canvasSize),
    )

    drawRoundRect(
      brush = bodyBrush,
      topLeft = Offset.Zero,
      size = Size(canvasSize, canvasSize),
      cornerRadius = CornerRadius(cornerRadius, cornerRadius),
      style = Fill,
    )

    // 3. Subtle Inner Bevel / Highlight Border
    drawRoundRect(
      color = Color(0x30FFFFFF),
      topLeft = Offset(2f, 2f),
      size = Size(canvasSize - 4f, canvasSize - 4f),
      cornerRadius = CornerRadius(cornerRadius - 2f, cornerRadius - 2f),
      style = Stroke(width = 3.5f),
    )

    // Outer subtle outline
    drawRoundRect(
      color = Color(0x18000000),
      topLeft = Offset.Zero,
      size = Size(canvasSize, canvasSize),
      cornerRadius = CornerRadius(cornerRadius, cornerRadius),
      style = Stroke(width = 2f),
    )

    // 4. Draw Pips (Dots)
    // Grid coordinate fractions
    val m = canvasSize * 0.26f // margin / near
    val c = canvasSize * 0.50f // center
    val f = canvasSize * 0.74f // far

    val standardPipRadius = canvasSize * 0.082f
    val centerPipRadius = if (dieValue == 1) canvasSize * 0.115f else standardPipRadius

    // Dot colors: Primary 1 is distinctive red pip; other pips are rich dark charcoal
    val pipColor = if (dieValue == 1) DiceRed else DicePipDark
    val pipHighlightColor = if (dieValue == 1) Color(0x40FFA4A4) else Color(0x30FFFFFF)

    // Pip coordinates per value
    val pipOffsets = when (dieValue) {
      1 -> listOf(Offset(c, c))
      2 -> listOf(Offset(m, m), Offset(f, f))
      3 -> listOf(Offset(m, m), Offset(c, c), Offset(f, f))
      4 -> listOf(Offset(m, m), Offset(f, m), Offset(m, f), Offset(f, f))
      5 -> listOf(Offset(m, m), Offset(f, m), Offset(c, c), Offset(m, f), Offset(f, f))
      6 -> listOf(
        Offset(m, m), Offset(m, c), Offset(m, f),
        Offset(f, m), Offset(f, c), Offset(f, f),
      )
      else -> listOf(Offset(c, c))
    }

    // Draw each pip with inset bevel & depth
    pipOffsets.forEach { pos ->
      // Inset dark shadow inside pip
      drawCircle(
        color = Color(0x22000000),
        radius = if (pos == Offset(c, c) && dieValue == 1) centerPipRadius + 1.5f else standardPipRadius + 1.5f,
        center = Offset(pos.x, pos.y + 1.5f),
      )
      // Main pip circle
      drawCircle(
        color = pipColor,
        radius = if (pos == Offset(c, c) && dieValue == 1) centerPipRadius else standardPipRadius,
        center = pos,
      )
      // Specular highlight in pip
      drawCircle(
        color = pipHighlightColor,
        radius = standardPipRadius * 0.35f,
        center = Offset(pos.x - standardPipRadius * 0.28f, pos.y - standardPipRadius * 0.28f),
      )
    }
  }
}

/**
 * Animated Shake Instruction Card with shaking phone icon.
 */
@Composable
fun ShakeInstructionCard(isRolling: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "shake_icon")
  val shakeRotation by infiniteTransition.animateFloat(
    initialValue = -12f,
    targetValue = 12f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 180, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "shake_anim",
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("shake_instruction_banner"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(DiceRed.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Vibration,
          contentDescription = null,
          tint = DiceRed,
          modifier = Modifier
            .size(24.dp)
            .graphicsLayer {
              if (!isRolling) {
                rotationZ = shakeRotation
              }
            },
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.shake_to_roll),
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Sensor threshold: 13.0 m/s²",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        )
      }

      Icon(
        imageVector = Icons.Outlined.Sensors,
        contentDescription = null,
        tint = AccentCyan,
        modifier = Modifier.size(22.dp),
      )
    }
  }
}

fun getDiceUnicode(value: Int): String {
  return when (value) {
    1 -> "⚀"
    2 -> "⚁"
    3 -> "⚂"
    4 -> "⚃"
    5 -> "⚄"
    6 -> "⚅"
    else -> "🎲"
  }
}

@Preview(showBackground = true)
@Composable
fun ShakeRollPreview() {
  MyApplicationTheme {
    ShakeRollApp()
  }
}
