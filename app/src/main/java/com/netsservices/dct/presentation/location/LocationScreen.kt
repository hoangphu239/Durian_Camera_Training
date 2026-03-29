package com.netsservices.dct.presentation.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.presentation.common.SearchMode

@Composable
fun LocationScreen(
    viewModel: LocationViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchModeDropdown(
            uiState = uiState,
            onSelect = {
                viewModel.setSearchMode(it)
                query = ""
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.isNotEmpty()) {
                    viewModel.searchRouter(it)
                }
            },
            label = { Text("Search...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            when (uiState.searchMode) {

                // ================= PLANTATION =================
                SearchMode.PLANTATION -> {
                    items(uiState.plantations) { plantation ->
                        ItemRow(
                            title = plantation.name,
                            subtitle = plantation.code
                        ) {
                        }
                    }
                }

                // ================= ORCHARD =================
                SearchMode.ORCHARD -> {
                    items(uiState.orchards) { orchard ->
                        ItemRow(
                            title = orchard.name,
                            subtitle = orchard.plantation.name
                        ) {
                        }
                    }
                }

                // ================= SITE =================
                SearchMode.SITE -> {
                    items(uiState.sites) { site ->
                        ItemRow(
                            title = site.name,
                            subtitle = "${site.orchard.name} • ${site.orchard.plantation.name}"
                        ) {
                            viewModel.selectSite(site)
                        }
                    }
                }
            }
        }

        if (uiState.selectSite != null) {
            SelectedLocation(uiState.selectSite)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchModeDropdown(
    uiState: LocationViewModel.UiState,
    onSelect: (SearchMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = uiState.searchMode.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Search by") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SearchMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name) },
                    onClick = {
                        expanded = false
                        onSelect(mode)
                    }
                )
            }
        }
    }
}

@Composable
fun ItemRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 10.dp, top = 10.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 15.sp)
    }
}

@Composable
fun SelectedLocation(site: Site) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.selected_location),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
        Text(
            "${site.name}, ${site.orchard.name}, ${site.orchard.plantation.name}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}