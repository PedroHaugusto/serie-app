package com.serie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF161826)
private val Text = Color(0xFFE9E9ED)
private val Accent = Color(0xFF9184D9)
private val Muted = Color(0xFFB2B6CA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(containerColor = Bg) { inner ->
                    Placeholder(Modifier.padding(inner))
                }
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "série",
            color = Text,
            fontSize = 40.sp,
            letterSpacing = (-1.4).sp,
        )
        Text(
            text = "Parte 0 · base pronta",
            color = Accent,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Pipeline validado: SDK, build e instalação.\nAs 10 telas entram a partir da Parte 1.",
            color = Muted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF161826, widthDp = 360, heightDp = 760)
@Composable
private fun PlaceholderPreview() {
    MaterialTheme { Placeholder() }
}
