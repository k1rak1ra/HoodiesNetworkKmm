import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import testObjects.ExampleRequestBody

@Composable
fun App() {
    val viewModel = SharedViewModel()
    viewModel.startServer()

    MaterialTheme {
        var text by remember { mutableStateOf(null as String?) }
        var bitmap by remember { mutableStateOf(null as ImageBitmap?) }
        var isLoading by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }


        viewModel.resultFlow.subscribe {
            isLoading = false
            text = it?.data
        }

        viewModel.imageFlow.subscribe {
            isLoading = false
            bitmap = it
        }

        viewModel.errorFlow.subscribe {
            isLoading = false

            it?.message ?: return@subscribe

            scope.launch {
                snackbarHostState.showSnackbar(it.message!!)
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { _ ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                Button(onClick = {
                    isLoading = true
                    viewModel.makeFailingRequest()
                }) {
                    Text("Send failing request")
                }

                Button(onClick = {
                    isLoading = true
                    viewModel.makeRequest(ExampleRequestBody("Hello, unified!"))
                }) {
                    Text("Send successful request")
                }

                text?.let { Text(it) }

                Button(onClick = {
                    isLoading = true
                    viewModel.getImage()
                }) {
                    Text("Load image")
                }

                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}