package org.openshouter.ui.places

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.data.PlaceEntity
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuLink
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection

@Composable
fun PlacesScreen(
    places: List<PlaceEntity>,
    onSavePlace: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    val homeLabel = stringResource(R.string.places_label_home)
    val workLabel = stringResource(R.string.places_label_work)
    MenuScaffold(stringResource(R.string.nav_places), scrollStore, "places", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody { Text(stringResource(R.string.places_help)) }
            MenuLink(stringResource(R.string.places_add_home), { onSavePlace(homeLabel) }, showDivider = true)
            MenuLink(stringResource(R.string.places_add_work), { onSavePlace(workLabel) }, showDivider = true)
        }
        if (places.isNotEmpty()) {
            MenuSection(stringResource(R.string.menu_section_list)) {
                places.forEachIndexed { index, place ->
                    MenuLink(
                        stringResource(R.string.places_item, place.label, place.radiusMeters.toInt()),
                        { onDelete(place.id) },
                        stringResource(R.string.places_delete),
                        showDivider = index > 0,
                    )
                }
            }
        }
    }
}
