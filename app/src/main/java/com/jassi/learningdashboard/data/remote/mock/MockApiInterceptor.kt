package com.jassi.learningdashboard.data.remote.mock

import android.content.res.AssetManager
import com.jassi.learningdashboard.core.network.NetworkMonitor
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject

class MockApiInterceptor @Inject constructor(
    private val assets: AssetManager,
    private val networkMonitor: NetworkMonitor,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!networkMonitor.isOnline()) {
            throw IOException("Mock transport: device is offline (${request.url})")
        }

        Thread.sleep(SIMULATED_LATENCY_MILLIS)

        val assetName = request.url.pathSegments.last().ifEmpty { COURSES_ASSET }
        val payload = runCatching { assets.open(assetName).use { it.readBytes() } }
            .getOrElse { return notFound(chain, assetName) }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (mock)")
            .body(payload.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun notFound(chain: Interceptor.Chain, assetName: String) = Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(404)
        .message("No mock asset named $assetName")
        .body("""{"error":"not_found"}""".toResponseBody(JSON_MEDIA_TYPE))
        .build()

    private companion object {
        const val SIMULATED_LATENCY_MILLIS = 900L
        const val COURSES_ASSET = "courses.json"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
