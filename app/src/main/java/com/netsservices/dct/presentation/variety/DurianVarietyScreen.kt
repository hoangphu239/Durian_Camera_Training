package com.netsservices.dct.presentation.variety

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.presentation.common.ConfigStep


@Composable
fun DurianVarietyScreen(
    viewModel: DurianVarietyViewModel = hiltViewModel(),
    countryCode: String
) {
    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 16.dp)
    ) {
        DurianTypesDropdown(
            uiState = uiState,
            onSelect = { durian ->
                viewModel.selectDurianVariety(durian)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        uiState.selectDurianVariety?.let { variety ->
            viewModel.updateAction(ConfigStep.DURIAN_TYPE.name)
            SelectedDurianVariety(variety)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getDurianVarieties(countryCode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurianTypesDropdown(
    uiState: DurianVarietyViewModel.UiState,
    onSelect: (DurianItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<DurianItem?>(null) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_durian_variety)) },
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
            uiState.durianVarieties.forEach { durian ->
                DropdownMenuItem(
                    text = { Text(durian.name) },
                    onClick = {
                        selected = durian
                        expanded = false
                        onSelect(durian)
                    }
                )
            }
        }
    }
}


@Composable
fun SelectedDurianVariety(durianVariety: DurianItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xFFE8F5E9))
            .padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.durian_variety) + ": ${durianVariety.name} (${durianVariety.localName})",
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.description) + ": ${durianVariety.description}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}