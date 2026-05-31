package com.schroepf.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RowApp(viewModel: RowViewModel = viewModel(factory = RowViewModel.factory())) {
    val state by viewModel.state.collectAsState()
    RowScreen(
        state = state,
        onRefresh = { viewModel.send(RowIntent.Refresh) }
    )
}

@Composable
fun RowScreen(
    state: RowState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = when {
            state.isLoading -> stringResource(id = R.string.loading)
            state.error != null -> state.error
            state.message.isNotBlank() -> state.message
            else -> ""
        }

        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRefresh
        ) {
            Text(text = stringResource(id = R.string.refresh))
        }
    }
}
