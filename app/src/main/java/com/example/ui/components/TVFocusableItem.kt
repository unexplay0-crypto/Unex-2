package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TVFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "tv_card",
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val active = isFocused || isHovered

    val scale by animateFloatAsState(targetValue = if (active) 1.05f else 1.0f, label = "card_scale")

    Surface(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .focusable(enabled, interactionSource)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        color = if (active) SurfaceCardElevated else SurfaceCard,
        border = BorderStroke(
            width = if (active) 2.5.dp else 1.dp,
            color = if (active) NeonCyan else Color.White.copy(alpha = 0.08f)
        ),
        shadowElevation = if (active) 12.dp else 2.dp
    ) {
        content()
    }
}

/**
 * TV in-app virtual QWERTY keyboard designed for D-Pad navigation.
 */
@Composable
fun TVVirtualKeyboardDialog(
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    val rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "_"),
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "@"),
        listOf("z", "x", "c", "v", "b", "n", "m", ".", ":", "/")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BackgroundDark,
            border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TECLADO VIRTUAL DE TV (D-PAD)",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Display input text
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (text.isEmpty()) "Digite aqui..." else text,
                        color = if (text.isEmpty()) TextSecondary else TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keyboard Rows
                rows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        rowKeys.forEach { key ->
                            TVFocusableCard(
                                onClick = { text += key },
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(38.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick shortcuts row for TV URLs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf("http://", ".com", ".net", ".m3u8", "ESPACE").forEach { shortcut ->
                        TVFocusableCard(
                            onClick = {
                                if (shortcut == "ESPACE") text += " "
                                else text += shortcut
                            },
                            modifier = Modifier
                                .padding(2.dp)
                                .height(36.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = shortcut,
                                    color = NeonPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Backspace, Clear, Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (text.isNotEmpty()) text = text.dropLast(1) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGold)
                    ) {
                        Icon(Icons.Default.Backspace, contentDescription = "Apagar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apagar")
                    }

                    OutlinedButton(onClick = { text = "" }) {
                        Text("Limpar", color = TextSecondary)
                    }

                    Button(
                        onClick = { onConfirm(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Parental PIN Control Dialog (+18 Protection, default 0000).
 */
@Composable
fun ParentalPinDialog(
    correctPin: String = "0000",
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundDark,
            border = BorderStroke(1.5.dp, NeonPurple),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Bloqueio Parental",
                    tint = NeonPurple,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CONTROLE PARENTAL (+18)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Digite o código PIN (Padrão: 0000)",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // PIN display dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFilled) NeonPurple else SurfaceCard,
                            border = BorderStroke(1.dp, if (hasError) Color.Red else NeonPurple.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isFilled) {
                                    Text("●", color = Color.White, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("PIN Incorreto! Tente novamente.", color = Color.Red, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric Keypad
                val numRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                numRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        row.forEach { digit ->
                            TVFocusableCard(
                                onClick = {
                                    when (digit) {
                                        "C" -> {
                                            enteredPin = ""
                                            hasError = false
                                        }
                                        "DEL" -> {
                                            if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                            hasError = false
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += digit
                                                hasError = false
                                                if (enteredPin.length == 4) {
                                                    if (enteredPin == correctPin) {
                                                        onUnlocked()
                                                    } else {
                                                        hasError = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(width = 54.dp, height = 44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = digit,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    }
}

/**
 * VOD Resume Modal (Retomada Inteligente).
 */
@Composable
fun VodResumeDialog(
    title: String,
    positionMs: Long,
    durationMs: Long,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onDismiss: () -> Unit
) {
    val posMinutes = positionMs / 60_000
    val posSeconds = (positionMs % 60_000) / 1000
    val timeFormatted = String.format("%02d:%02d", posMinutes, posSeconds)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundDark,
            border = BorderStroke(1.5.dp, NeonCyan),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RETOMAR REPRODUÇÃO",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Você já assistiu até $timeFormatted. Como deseja continuar?",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Do Início")
                    }

                    Button(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retomar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
