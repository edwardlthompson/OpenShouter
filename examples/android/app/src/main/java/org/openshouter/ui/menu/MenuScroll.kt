package org.openshouter.ui.menu

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingLg
import dev.foss.goldenpath.ui.theme.SpacingMd

class MenuScrollStore {
    private val offsets = mutableMapOf<String, Int>()
    fun offset(key: String): Int = offsets[key] ?: 0
    fun update(key: String, value: Int) {
        offsets[key] = value
    }
}

@Composable
fun rememberMenuScrollStore(): MenuScrollStore = remember { MenuScrollStore() }

@Composable
fun rememberMenuScroll(store: MenuScrollStore, key: String): ScrollState {
    val state = rememberScrollState(store.offset(key))
    LaunchedEffect(key, state) {
        snapshotFlow { state.value }.collect { store.update(key, it) }
    }
    return state
}

@Composable
fun rememberMenuListScroll(store: MenuScrollStore, key: String): LazyListState {
    val state = rememberLazyListState(store.offset(key))
    LaunchedEffect(key, state) {
        snapshotFlow { state.firstVisibleItemIndex }.collect { store.update(key, it) }
    }
    return state
}

@Composable
fun MenuClose(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bottomInsetPadding(),
    ) {
        Text(stringResource(R.string.settings_close))
    }
}

@Composable
fun MenuScaffold(
    title: String,
    scrollStore: MenuScrollStore,
    scrollKey: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberMenuScroll(scrollStore, scrollKey))
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        content()
        if (onBack != null) MenuClose(onBack)
    }
}
