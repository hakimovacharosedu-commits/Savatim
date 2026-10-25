package uz.savatim.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.savatim.app.R
import uz.savatim.app.ui.theme.Inter
import uz.savatim.app.ui.theme.SColors

// ---------------------------------------------------------------- buttons (Kangsayur: 54dp, radius 10)

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(10.dp), ambientColor = Color(0x12000000)),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SColors.Primary,
            contentColor = SColors.White,
            disabledContainerColor = SColors.Primary.copy(alpha = 0.45f),
            disabledContentColor = SColors.White,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(color = SColors.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge, color = SColors.White)
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SColors.Primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SColors.Primary),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = SColors.Primary)
    }
}

@Composable
fun WhiteButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SColors.White, contentColor = SColors.Primary),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = SColors.Primary)
    }
}

// ---------------------------------------------------------------- inputs (Kangsayur: label + gray field)

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    Column(modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = SColors.Gray1)
            Spacer(Modifier.height(8.dp))
        }
        InputBox(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = imeAction,
            onDone = onDone,
        )
    }
}

@Composable
fun InputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    onDone: () -> Unit = {},
) {
    var visible by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(fontFamily = Inter, fontSize = 15.sp, color = SColors.Dark),
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            capitalization = capitalization,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }, onSearch = { onDone() }, onGo = { onDone() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(if (singleLine) 48.dp else 88.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SColors.Field)
                    .padding(horizontal = 14.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            ) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(10.dp))
                }
                Box(Modifier.weight(1f).padding(vertical = if (singleLine) 0.dp else 12.dp)) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = SColors.Gray4)
                    }
                    inner()
                }
                if (isPassword) {
                    Icon(
                        if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = SColors.Gray4,
                        modifier = Modifier.size(22.dp).clickable { visible = !visible },
                    )
                }
            }
        },
    )
}

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    InputBox(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        imeAction = ImeAction.Search,
        leading = { Icon(Icons.Rounded.Search, null, tint = SColors.GrayText, modifier = Modifier.size(20.dp)) },
    )
}

// ---------------------------------------------------------------- brand

@Composable
fun Logo(color: Color = SColors.White, size: Int = 30) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Savatim",
            color = color,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = size.sp,
        )
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Rounded.ShoppingBasket, null, tint = color, modifier = Modifier.size((size * 0.95f).dp))
    }
}

/** Simple illustration: an emoji inside a soft circle. [onGreen] = drawn on a green background. */
@Composable
fun Art(size: Dp, modifier: Modifier = Modifier, emoji: String = "🧺", onGreen: Boolean = false) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(if (onGreen) SColors.White.copy(alpha = 0.22f) else SColors.PrimarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.48f).sp)
    }
}

/** The big green curved shape at the top of the Kangsayur auth screens (ellipse 596×489 at -158,-112). */
@Composable
fun GreenCurveHeader(height: Dp, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxWidth().height(height)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val scale = w / 375f
            drawOval(
                color = SColors.Primary,
                topLeft = Offset(-158f * scale, size.height - 489f * scale),
                size = Size(596f * scale, 489f * scale),
            )
        }
        content()
    }
}

@Composable
fun BackButton(onClick: () -> Unit, tint: Color = SColors.Dark) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ---------------------------------------------------------------- tiles & chips

@Composable
fun EmojiTile(emoji: String, size: Dp, modifier: Modifier = Modifier, background: Color = SColors.PrimarySoft) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp)
    }
}

/** Kangsayur-style category item: round picture + label below. */
@Composable
fun CategoryChip(emoji: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SColors.PrimarySoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SColors.White)
                .border(1.dp, if (selected) SColors.Primary else SColors.Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 24.sp) }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) SColors.Primary else SColors.Gray1,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
fun Avatar(emoji: String, size: Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(SColors.PrimarySoft),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = (size.value * 0.5f).sp) }
}

@Composable
fun Badge(text: String, color: Color = SColors.Primary) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
fun EmptyState(title: String, text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Art(120.dp)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = SColors.GrayText, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}
