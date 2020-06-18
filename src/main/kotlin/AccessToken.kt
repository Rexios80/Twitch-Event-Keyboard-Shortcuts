import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AccessToken(private val CLIENT_ID: String, private val CLIENT_SECRET: String, private val client: OkHttpClient) {
    private val URL: String = "https://id.twitch.tv/oauth2/token?client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&grant_type=client_credentials"
    private var ACCESS_TOKEN: String? = null
    private var expires: Long = 0
    private var stored: Long = 0
    val accessToken: String? get() = if (System.currentTimeMillis() - stored >= expires) generateToken() else ACCESS_TOKEN

    private fun generateToken(): String? {
        val request = Request.Builder()
            .url(URL)
            .post(byteArrayOf().toRequestBody(null, 0, byteArrayOf().size))
            .build()
        try {
            val call = client.newCall(request)
            val response = call.execute()
            val `object` = JSONObject(response.body!!.string())
            stored = System.currentTimeMillis()
            ACCESS_TOKEN = `object`.getString("access_token")
            expires = `object`.getLong("expires_in")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return ACCESS_TOKEN
    }

    init {
        generateToken()
    }
}