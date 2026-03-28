package com.netsservices.dct.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.netsservices.dct.presentation.common.SearchMode
import com.netsservices.dct.presentation.common.SearchStep


@Composable
fun HomeScreen(viewModel: HomeViewModel) {
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
                viewModel.searchRouter(it)
            },
            label = { Text("Search...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            when (uiState.currentStep) {

                // ================= PLANTATION =================
                SearchStep.PLANTATION -> {
                    items(uiState.plantations) { plantation ->
                        ItemRow(
                            title = plantation.name,
                            subtitle = plantation.code
                        ) {
                            viewModel.selectPlantation(plantation)
                        }
                    }
                }

                // ================= ORCHARD =================
                SearchStep.ORCHARD -> {
                    items(uiState.orchards) { orchard ->
                        ItemRow(
                            title = orchard.name,
                            subtitle = orchard.plantation.name
                        ) {
                            viewModel.selectOrchard(orchard)
                        }
                    }
                }

                // ================= SITE =================
                SearchStep.SITE -> {
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

        if (uiState.siteId.isNotEmpty()) {
            SelectedLocation(uiState)
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchModeDropdown(
    uiState: HomeViewModel.UiState,
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
            .padding(12.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SelectedLocation(uiState: HomeViewModel.UiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color(0xFFE8F5E9))
    ) {
        Text("Selected Location", fontWeight = FontWeight.Bold)
        Text("Plantation: ${uiState.plantationId}")
        Text("Orchard: ${uiState.orchardId}")
        Text("Site: ${uiState.siteId}")
    }
}