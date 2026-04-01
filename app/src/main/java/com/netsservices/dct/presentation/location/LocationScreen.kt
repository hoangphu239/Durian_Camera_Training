package com.netsservices.dct.presentation.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.components.AppTextField


@Composable
fun LocationScreen(
    countryName: String,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    var query by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 16.dp)
    ) {
        AppTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.isNotEmpty()) {
                    query = it
                    viewModel.onQueryChanged(it)
                }
            },
            hint = stringResource(R.string.search_site),
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.sites) { site ->
                    ItemRow(
                        title = site.name,
                        subtitle = "${site.orchard.name} • ${site.orchard.plantation.name}"
                    ) {
                        keyboardController?.hide()
                        if(site != viewModel.getCurrentSite()) {
                            viewModel.updateAction(ConfigStep.SITE.name)
                            viewModel.selectSite(site)
                        }
                    }
                }
            }
        }

        uiState.selectSite?.let { site ->
            SelectedLocation(countryName, site)
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
fun SelectedLocation(
    countryName: String,
    site: Site
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xFFE8F5E9))
            .padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.site, site.name),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.orchard, site.orchard.name),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.plantation, site.orchard.plantation.name),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.country) + ": $countryName",
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}