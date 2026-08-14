package org.openshouter.ui.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.data.PlaceEntity

@Composable
fun PlacesScreen(
    places: List<PlaceEntity>,
    onSavePlace: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(stringResource(R.string.nav_places), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.places_help))
        val homeLabel = stringResource(R.string.places_label_home)
        val workLabel = stringResource(R.string.places_label_work)
        places.forEach { place ->
            Text(stringResource(R.string.places_item, place.label, place.radiusMeters.toInt()))
            Button(onClick = { onDelete(place.id) }) {
                Text(stringResource(R.string.places_delete))
            }
        }
        Button(onClick = { onSavePlace(homeLabel) }) { Text(stringResource(R.string.places_add_home)) }
        Button(onClick = { onSavePlace(workLabel) }) { Text(stringResource(R.string.places_add_work)) }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}
