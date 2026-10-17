package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.storage.AppStorage
import com.example.ui.components.LowPolyBackground
import com.example.ui.components.TVFocusableCard
import com.example.ui.components.TVVirtualKeyboardDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    appStorage: AppStorage,
    onLoginSuccess: () -> Unit
) {
    val session = appStorage.getSession()
    val scope = rememberCoroutineScope()

    var serverUrl by remember { mutableStateOf(session.serverUrl) }
    var username by remember { mutableStateOf(session.username) }
    var password by remember { mutableStateOf(session.password) }
    var isLoading by remember { mutableStateOf(false) }

    // TV Virtual keyboard integration
    var activeKeyboardField by remember { mutableStateOf<String?>(null) } // "url", "user", "pass"

    fun performLogin(url: String, u: String, p: String) {
        isLoading = true
        scope.launch {
            delay(600) // Fast 600ms boot verification
            appStorage.saveSession(url, u, p)
            isLoading = false
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        LowPolyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding
            Surface(
                shape = CircleShape,
                color = NeonCyan.copy(alpha = 0.15f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = "UNEX Vision",
                        tint = NeonCyan,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("UNEX", color = NeonCyan, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                Text(" VISION", color = TextPrimary, fontWeight = FontWeight.Light, fontSize = 26.sp)
            }
            Text("Cinema & IPTV Streaming Engine • 10-Foot UI", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Login Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceDark.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("CONECTAR SERVIDOR XTREAM / M3U", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Server URL Input
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("URL do Servidor (ex: http://ip:port)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            IconButton(onClick = { activeKeyboardField = "url" }) {
                                Icon(Icons.Default.Keyboard, contentDescription = "Teclado TV", tint = TextSecondary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuário", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            IconButton(onClick = { activeKeyboardField = "user" }) {
                                Icon(Icons.Default.Keyboard, contentDescription = "Teclado TV", tint = TextSecondary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            IconButton(onClick = { activeKeyboardField = "pass" }) {
                                Icon(Icons.Default.Keyboard, contentDescription = "Teclado TV", tint = TextSecondary)
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connect Button
                    Button(
                        onClick = { performLogin(serverUrl, username, password) },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Entrar e Carregar Catálogo", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Instant Demo Server Access
                    OutlinedButton(
                        onClick = {
                            serverUrl = "http://live.unex-stream.net:8080"
                            username = "vip_demo"
                            password = "unex"
                            performLogin(serverUrl, username, password)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Acesso Rápido VIP (Demonstração 4K)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Virtual Keyboard
        activeKeyboardField?.let { field ->
            val initial = when (field) {
                "url" -> serverUrl
                "user" -> username
                "pass" -> password
                else -> ""
            }
            TVVirtualKeyboardDialog(
                initialText = initial,
                onConfirm = { result ->
                    when (field) {
                        "url" -> serverUrl = result
                        "user" -> username = result
                        "pass" -> password = result
                    }
                    activeKeyboardField = null
                },
                onDismiss = { activeKeyboardField = null }
            )
        }
    }
}
