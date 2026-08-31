package org.openshouter.ui.astro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.foss.goldenpath.R
import org.openshouter.astro.model.AstroAlarm
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun AstroAlarmLockscreenView(
    alarm: AstroAlarm,
    onSnooze: () -> Unit,
    onStop: () -> Unit
) {
    val currentTime = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    var showMathChallenge by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTime,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = alarm.label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.astro_action_snooze, alarm.snoozeMinutes),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (alarm.mathUnlockEnabled) {
                            showMathChallenge = true
                        } else {
                            onStop()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.astro_action_stop),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showMathChallenge) {
        MathChallengeDialog(
            onDismiss = { showMathChallenge = false },
            onSolved = {
                showMathChallenge = false
                onStop()
            }
        )
    }
}

@Composable
private fun MathChallengeDialog(
    onDismiss: () -> Unit,
    onSolved: () -> Unit
) {
    var num1 by remember { mutableStateOf(Random.nextInt(12, 50)) }
    var num2 by remember { mutableStateOf(Random.nextInt(11, 40)) }
    var isAddition by remember { mutableStateOf(Random.nextBoolean()) }
    var userAnswer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val correctAnswer = if (isAddition) num1 + num2 else num1 - num2
    val operator = if (isAddition) "+" else "-"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.astro_math_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$num1 $operator $num2 = ?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = {
                        userAnswer = it.filter { char -> char.isDigit() || char == '-' }
                        isError = false
                    },
                    label = { Text(stringResource(R.string.astro_math_hint)) },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (userAnswer.trim().toIntOrNull() == correctAnswer) {
                                onSolved()
                            } else {
                                isError = true
                                userAnswer = ""
                                num1 = Random.nextInt(15, 60)
                                num2 = Random.nextInt(12, 45)
                                isAddition = Random.nextBoolean()
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = stringResource(R.string.astro_math_incorrect),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.astro_action_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (userAnswer.trim().toIntOrNull() == correctAnswer) {
                                onSolved()
                            } else {
                                isError = true
                                userAnswer = ""
                                num1 = Random.nextInt(15, 60)
                                num2 = Random.nextInt(12, 45)
                                isAddition = Random.nextBoolean()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.astro_math_submit))
                    }
                }
            }
        }
    }
}
