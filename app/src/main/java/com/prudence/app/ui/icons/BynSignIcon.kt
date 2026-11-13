package com.prudence.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The official graphical sign of the Belarusian ruble (BYN): a Cyrillic "Б" with a horizontal
 * stroke. Approved by the National Bank of Belarus on 27 Jan 2026. Converted from the official
 * SVG at https://www.nbrb.by/coinsbanknotes/byn-ico (360.67 x 446.4 viewport; the SVG's
 * translate(-114.94, -213.77) is baked into the absolute coordinates below).
 */
val BynSignIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "BynSign",
        defaultWidth = 29.dp,
        defaultHeight = 36.dp,
        viewportWidth = 360.67f,
        viewportHeight = 446.4f,
    ).addPath(
        pathData = addPathNodes(
            "M360.67,315.07c0-72.5-62.75-131.27-140.16-131.27H112.64V49.6H311.06v-49.6H63.06v290h-63.1v49.7H63.06V446.4h49.54l107.92-.07c77.36,0,140.11-58.77,140.11-131.26Zm-248-25.1V233.33c35.89,0,72.35.07,107.87.07,50,0,90.56,36.57,90.56,81.67s-40.54,81.67-90.56,81.7l-107.87,0V339.67h112.7v-49.7Z"
        ),
        fill = SolidColor(Color.Black),
    ).build()
}
