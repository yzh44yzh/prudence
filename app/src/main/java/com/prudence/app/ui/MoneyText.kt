package com.prudence.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.prudence.app.domain.Money
import com.prudence.app.ui.icons.BynSignIcon

private const val BYN = "BYN"
private const val BYN_INLINE_ID = "byn-sign"

/**
 * An amount rendered for display. BYN uses the official graphical sign (a vector, since the sign
 * has no Unicode codepoint) placed inline after the amount; other currencies are plain text via
 * [Money.format].
 */
@Composable
fun MoneyText(
    amountMinor: Long,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
) {
    if (currencySymbol != BYN) {
        Text(Money.format(amountMinor, currencySymbol), modifier = modifier, style = style, textAlign = textAlign)
        return
    }

    val fontSize = style.fontSize.takeIf { it != TextUnit.Unspecified } ?: 16.sp
    val text: AnnotatedString = buildAnnotatedString {
        append(Money.format(amountMinor)) // plain number; the sign is the inline glyph
        append(' ')
        appendInlineContent(BYN_INLINE_ID, BYN)
    }
    val inlineContent = mapOf(
        BYN_INLINE_ID to InlineTextContent(
            Placeholder(
                width = fontSize * 0.65f, // glyph aspect 360.67 : 446.4
                height = fontSize * 0.9f,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            Icon(
                BynSignIcon,
                contentDescription = BYN,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
    Text(text = text, modifier = modifier, style = style, textAlign = textAlign, inlineContent = inlineContent)
}
