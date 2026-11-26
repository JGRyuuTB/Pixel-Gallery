package com.GalleryYou.Pixel.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.GalleryYou.Pixel.ui.components.NavBarScrubState
import com.GalleryYou.Pixel.ui.components.navBarScrub
import com.GalleryYou.Pixel.ui.components.navBarScrubItem

internal data class HomeNavTab(
    val id: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

internal val HOME_FLOATING_RAIL_RESERVE: Dp = 88.dp
internal val HOME_STANDARD_RAIL_RESERVE: Dp = 80.dp

/**
 * The expressive Home toolbar stood on its end for windows 600dp and wider.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeFloatingRail(
    tabs: List<HomeNavTab>,
    selectedTab: Int,
    scrub: NavBarScrubState,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalFloatingToolbar(
        expanded = true,
        modifier = modifier.navBarScrub(scrub, vertical = true),
    ) {
        tabs.forEach { tab ->
            val selected = (scrub.hoveredTab ?: selectedTab) == tab.id
            val container by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "railContainer",
            )
            val content by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "railContent",
            )
            Surface(
                selected = selected,
                onClick = { onSelect(tab.id) },
                shape = RoundedCornerShape(28.dp),
                color = container,
                contentColor = content,
                modifier = Modifier
                    .width(56.dp)
                    .navBarScrubItem(scrub, tab.id),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NavTabIcon(tab = tab, selected = selected)
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn() + expandVertically(
                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        ),
                        exit = fadeOut() + shrinkVertically(
                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                        ),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeStandardRail(
    tabs: List<HomeNavTab>,
    selectedTab: Int,
    scrub: NavBarScrubState,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .navBarScrub(scrub, vertical = true),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start),
    ) {
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEach { tab ->
                val selected = (scrub.hoveredTab ?: selectedTab) == tab.id
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelect(tab.id) },
                    icon = { NavTabIcon(tab = tab, selected = selected) },
                    label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.navBarScrubItem(scrub, tab.id),
                )
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
internal fun NavTabIcon(tab: HomeNavTab, selected: Boolean) {
    if (tab.badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(if (tab.badgeCount > 99) "99+" else tab.badgeCount.toString())
                }
            },
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.icon,
                contentDescription = tab.label,
                modifier = Modifier.size(24.dp),
            )
        }
    } else {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.icon,
                contentDescription = tab.label,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
