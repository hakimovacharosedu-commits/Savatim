package uz.savatim.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uz.savatim.app.R

// Colors taken from the Kangsayur + Nectar Figma files
object SColors {
    val Primary = Color(0xFF4CAD73)        // Kangsayur "primary"
    val PrimaryDark = Color(0xFF489E67)    // Nectar price badge
    val Link = Color(0xFF0EB177)           // Kangsayur "Brand Color Green"
    val PrimarySoft = Color(0xFFE6F4EC)    // light green tiles / selected chips
    val PrimaryCircle = Color(0xFF6DBE8E)  // lighter ellipse on green screens
    val Dark = Color(0xFF181725)           // Nectar "Dark Deep"
    val Gray1 = Color(0xFF333333)          // Kangsayur "Gray 1"
    val GrayText = Color(0xFF7C7C7C)       // Nectar secondary text
    val Gray4 = Color(0xFFBDBDBD)          // Kangsayur "Gray 4" (placeholders)
    val Field = Color(0xFFF2F2F2)          // Kangsayur "Gray 6" (input background)
    val Border = Color(0xFFE2E2E2)
    val Divider = Color(0xFFEFEFEF)
    val Danger = Color(0xFFE5484D)
    val White = Color.White
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private val base = TextStyle(fontFamily = Inter)

val SavatimTypography = Typography(
    displaySmall = base.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 41.sp),
    headlineMedium = base.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    headlineSmall = base.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleLarge = base.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
    titleMedium = base.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    titleSmall = base.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp),
    bodyLarge = base.copy(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    bodyMedium = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    bodySmall = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    labelLarge = base.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    labelMedium = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp),
    labelSmall = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
)

private val colorScheme = lightColorScheme(
    primary = SColors.Primary,
    onPrimary = SColors.White,
    primaryContainer = SColors.PrimarySoft,
    onPrimaryContainer = SColors.Dark,
    secondary = SColors.Link,
    background = SColors.White,
    onBackground = SColors.Dark,
    surface = SColors.White,
    onSurface = SColors.Dark,
    surfaceVariant = SColors.Field,
    onSurfaceVariant = SColors.GrayText,
    outline = SColors.Border,
    outlineVariant = SColors.Divider,
    error = SColors.Danger,
    surfaceTint = SColors.White,
    surfaceContainerLowest = SColors.White,
    surfaceContainerLow = SColors.White,
    surfaceContainer = SColors.White,
    surfaceContainerHigh = SColors.White,
    surfaceContainerHighest = SColors.Field,
    secondaryContainer = SColors.PrimarySoft,
    onSecondaryContainer = SColors.Dark,
)

@Composable
fun SavatimTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, typography = SavatimTypography, content = content)
}
