package com.example.campusfix

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BackButton(
    onBackClick: () -> Unit
) {

    Button(
        onClick = onBackClick
    ) {

        Text("← Back")
    }
}
