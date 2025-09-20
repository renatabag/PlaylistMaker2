package com.example.playlistmaker.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.states.FavouritesState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FavoritesViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel(), KoinComponent {

    private val trackUiMapper: TrackUiMapper by inject()

    val favoritesState: StateFlow<FavouritesState> = favoriteTracksInteractor.getAllFavorites()
        .map { tracks ->
            Log.d("FavoritesViewModel", "Flow emitted ${tracks.size} tracks")
            val tracksUi = trackUiMapper.mapListToUi(tracks)

            if (tracksUi.isEmpty()) {
                Log.d("FavoritesViewModel", "Setting empty state")
                FavouritesState.Empty
            } else {
                Log.d("FavoritesViewModel", "Setting content state with ${tracksUi.size} tracks")
                FavouritesState.Content(tracksUi)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavouritesState.Empty
        )

    suspend fun isFavorite(trackId: Int): Boolean {
        return favoriteTracksInteractor.isFavorite(trackId)
    }

    fun addToFavorites(track: com.example.playlistmaker.domain.models.Track) {
        viewModelScope.launch {
            favoriteTracksInteractor.addToFavorites(track)
        }
    }

    fun removeFromFavorites(track: com.example.playlistmaker.domain.models.Track) {
        viewModelScope.launch {
            favoriteTracksInteractor.removeFromFavorites(track)
        }
    }
}