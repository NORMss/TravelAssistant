package com.normno.travelassistant.core.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.normno.travelassistant.core.designsystem.component.ActionChip
import com.normno.travelassistant.core.model.Address

/**
 * Действия над адресом — самая используемая часть прошлого приложения, перенесённая
 * без изменений по смыслу.
 *
 * Суть: таксисту нужны не наши буквы, а местные, и копировать их надо одним касанием.
 * Поэтому «Скопировать» кладёт в буфер [Address.copyText] — местное написание, а не
 * понятную нам подпись.
 *
 * Список приложений-получателей сознательно не зашит: вместо кнопок под конкретные
 * сервисы отдаём системе `geo:`-интент и шер. Для универсального приложения это
 * единственный рабочий путь — набор нужных приложений меняется от страны к стране.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddressActions(
    address: Address,
    modifier: Modifier = Modifier,
    onCopied: (String) -> Unit = {},
) {
    val context = LocalContext.current

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (address.copyText.isNotBlank()) {
            ActionChip(
                label = "Скопировать",
                icon = Icons.Rounded.ContentCopy,
                onClick = {
                    context.copyToClipboard(address.copyText)
                    onCopied(address.copyText)
                },
            )
        }

        val point = address.point
        if (point != null) {
            ActionChip(
                label = "На карте",
                icon = Icons.Rounded.Map,
                onClick = {
                    // label в geo: нужен, чтобы в чужом приложении точка была подписана
                    val label = Uri.encode(address.display.ifBlank { address.local })
                    context.openUri("geo:${point.lat},${point.lng}?q=${point.lat},${point.lng}($label)")
                },
            )
        } else if (address.local.isNotBlank()) {
            // Без координат ищем по местному написанию: локальные карты найдут точнее.
            ActionChip(
                label = "Найти",
                icon = Icons.Rounded.Map,
                onClick = { context.openUri("geo:0,0?q=${Uri.encode(address.local)}") },
            )
        }

        if (address.copyText.isNotBlank()) {
            ActionChip(
                label = "Отправить",
                icon = Icons.Rounded.Share,
                onClick = { context.share(address.copyText) },
            )
        }
    }
}

private fun Context.copyToClipboard(text: String) {
    getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(text, text))
}

private fun Context.openUri(uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    // Карты может не быть вовсе — на телефоне без сервисов Google это обычное дело.
    runCatching { startActivity(intent) }
}

private fun Context.share(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching { startActivity(Intent.createChooser(intent, null)) }
}
