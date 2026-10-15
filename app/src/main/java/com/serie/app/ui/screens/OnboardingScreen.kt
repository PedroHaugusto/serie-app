package com.serie.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.PhIcon
import com.serie.app.ui.components.SerieBlockButton
import com.serie.app.ui.components.SerieButtonStyle
import com.serie.app.ui.theme.Dimens
import com.serie.app.ui.theme.Nocturne
import com.serie.app.ui.theme.accentGlow

/** 01 · Onboarding - a porta de entrada do app. */
@Composable
fun OnboardingScreen(onEnter: () -> Unit, modifier: Modifier = Modifier) {
    val c = Nocturne.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .padding(horizontal = Dimens.screenMargin),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .accentGlow(RoundedCornerShape(16.dp), alpha = 0.28f)
                .clip(RoundedCornerShape(16.dp))
                .background(c.surface),
            contentAlignment = Alignment.Center,
        ) { PhIcon(Ph.barbell, size = 28.dp, tint = c.accent) }

        Text(
            "série",
            style = Nocturne.type.h1,
            color = c.text,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            "Treino · carga · evolução",
            style = Nocturne.type.caption,
            color = c.accentRamp.s300,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            "Cada repetição, registrada.",
            style = Nocturne.type.display,
            color = c.text,
            modifier = Modifier.padding(top = 32.dp),
        )
        Text(
            "Monte seus treinos, registre série a série e veja sua força " +
                "crescer semana após semana.",
            style = Nocturne.type.body,
            color = c.textSecondary,
            modifier = Modifier.padding(top = 14.dp),
        )

        Column(
            modifier = Modifier.padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SerieBlockButton(
                label = "Continuar com Google",
                onClick = onEnter,
                leadingIcon = Ph.googleLogo,
            )
            SerieBlockButton(
                label = "Entrar com e-mail",
                onClick = onEnter,
                style = SerieButtonStyle.Secondary,
                leadingIcon = Ph.envelopeSimple,
            )
            Text(
                "Ao continuar, você aceita os Termos de uso e a Política de privacidade.",
                style = Nocturne.type.caption,
                color = c.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
