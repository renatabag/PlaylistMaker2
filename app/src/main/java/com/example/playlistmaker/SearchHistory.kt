import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxHistorySize = 10

    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > maxHistorySize) {
            history.subList(maxHistorySize, history.size).clear()
        }
        saveHistory(history)
    }

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString("history", null)
        return if (json == null) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type)
        }
    }

    fun clearHistory() {
        sharedPreferences.edit().remove("history").apply()
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString("history", json).apply()
    }
}