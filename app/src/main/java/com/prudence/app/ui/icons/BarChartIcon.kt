package com.prudence.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Three-bar chart glyph (the core icon set has none). Tinted by the surrounding Icon. */
val BarChartIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "BarChart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes("M4,20 L4,10 L7,10 L7,20 Z M10.5,20 L10.5,4 L13.5,4 L13.5,20 Z M17,20 L17,14 L20,14 L20,20 Z"),
        fill = SolidColor(Color.Black),
    ).build()
}
