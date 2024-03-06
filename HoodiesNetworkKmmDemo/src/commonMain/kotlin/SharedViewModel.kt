import androidx.compose.ui.graphics.ImageBitmap
import net.k1ra.hoodies_network_kmm.HoodiesNetworkClient
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import helpers.asStateFlowClass
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import testObjects.ExampleRequestBody
import testObjects.HttpBinResponse
import kotlin.time.Duration.Companion.minutes

class SharedViewModel {
    private val _resultFlow = MutableStateFlow<HttpBinResponse?>(null)
    val resultFlow = _resultFlow.asStateFlowClass()

    private val _imageFlow = MutableStateFlow<ImageBitmap?>(null)
    val imageFlow = _imageFlow.asStateFlowClass()

    private val _errorFlow = MutableStateFlow<HttpClientError?>(null)
    val errorFlow = _errorFlow.asStateFlowClass()

    private val client = HoodiesNetworkClient.Builder().apply {
        baseUrl = "http://127.0.0.1:6969/"
        cacheConfiguration = CacheEnabled(staleDataThreshold = 1.minutes, encryptionEnabled = true)
    }.build()

    fun startServer() =  CoroutineScope(Dispatchers.IO).launch {
        ServerManager.start()
    }

    fun makeRequest(body: ExampleRequestBody) =  CoroutineScope(Dispatchers.IO).launch {
        when (val result = client.post<HttpBinResponse, ExampleRequestBody>("post", body)) {
            is Success -> {
                println("Request NetworkTime: ${result.rawResponse?.networkTimeMs}")
                _resultFlow.value = result.value
            }
            is Failure -> _errorFlow.value = result.reason
        }
    }

    fun makeFailingRequest() =  CoroutineScope(Dispatchers.IO).launch {
        when (val result = client.post<HttpBinResponse, ExampleRequestBody>("doesNotExist", ExampleRequestBody("test"))) {
            is Success -> {
                println("Fail NetworkTime: ${result.rawResponse?.networkTimeMs}")
                _resultFlow.value = result.value
            }
            is Failure -> _errorFlow.value = result.reason
        }
    }

    fun getImage() = CoroutineScope(Dispatchers.IO).launch {
        when (val result = client.get<ImageBitmap>("image")) {
            is Success -> {
                println("Image NetworkTime: ${result.rawResponse?.networkTimeMs}")
                _imageFlow.value = result.value
            }
            is Failure -> _errorFlow.value = result.reason
        }
    }
}