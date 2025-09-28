package com.example.playlistmaker.presentation.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.PlayerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewPlaylistFragment : Fragment() {

    private lateinit var createButton: FrameLayout
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var nameFrame: FrameLayout
    private lateinit var descriptionFrame: FrameLayout
    private lateinit var nameLabel: TextView
    private lateinit var descriptionLabel: TextView
    private lateinit var coverImageButton: ImageButton
    private lateinit var blueButtonDrawable: Drawable
    private lateinit var grayButtonDrawable: Drawable
    private lateinit var blueFrameDrawable: Drawable
    private lateinit var grayFrameDrawable: Drawable

    private val playlistInteractor: PlaylistInteractor by inject()
    private val playerViewModel: PlayerViewModel by viewModel() // Добавляем ViewModel

    private var selectedImageUri: Uri? = null
    private var savedImagePath: String? = null
    private var track: TrackUi? = null

    // Флаг для отслеживания наличия несохраненных данных
    private var hasUnsavedChanges: Boolean = false

    // Универсальный способ выбора изображения
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("PhotoPicker", "Selected URI: $uri")
                coverImageButton.setImageURI(uri)
                coverImageButton.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                selectedImageUri = uri
                savedImagePath = saveImageToPrivateStorage(uri)
                hasUnsavedChanges = true
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    companion object {
        private const val ARG_TRACK = "track"

        fun newInstance(track: TrackUi?): NewPlaylistFragment {
            return NewPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TRACK, track)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = arguments?.getParcelable(ARG_TRACK)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.new_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        blueButtonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.blue_frame)!!
        grayButtonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gray_frame)!!
        blueFrameDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.frame_border_blue)!!
        grayFrameDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.frame_border_grey)!!

        createButton = view.findViewById(R.id.create_playlist_button)
        nameEditText = view.findViewById(R.id.name_playlist)
        descriptionEditText = view.findViewById(R.id.description_playlist)
        nameFrame = view.findViewById(R.id.name_frame)
        descriptionFrame = view.findViewById(R.id.description_frame)
        nameLabel = view.findViewById(R.id.name_label)
        descriptionLabel = view.findViewById(R.id.description_label)
        coverImageButton = view.findViewById(R.id.add_picture)

        view.findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            checkForUnsavedChangesAndNavigateBack()
        }

        coverImageButton.setOnClickListener {
            openImagePicker()
        }

        setupFocusListeners()
        setupTextWatchers()
        updateCreateButtonState(false)
    }

    override fun onResume() {
        super.onResume()
        setBottomNavigationVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        checkForUnsavedChanges()
        setBottomNavigationVisibility(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        setBottomNavigationVisibility(true)
    }

    private fun setBottomNavigationVisibility(visible: Boolean) {
        try {
            val activity = requireActivity()
            val bottomNav = activity.findViewById<View>(R.id.bottom_navigation)
            bottomNav?.visibility = if (visible) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e("NewPlaylistFragment", "Error setting bottom navigation visibility", e)
        }
    }

    private fun checkForUnsavedChanges() {
        val hasName = nameEditText.text.toString().trim().isNotEmpty()
        val hasDescription = descriptionEditText.text.toString().trim().isNotEmpty()
        val hasImage = savedImagePath != null
        hasUnsavedChanges = hasName || hasDescription || hasImage
    }

    private fun checkForUnsavedChangesAndNavigateBack() {
        checkForUnsavedChanges()
        if (hasUnsavedChanges) {
            showExitConfirmationDialog()
        } else {
            navigateBack()
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохраненные данные будут потеряны")
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Завершить") { dialog, _ ->
                dialog.dismiss()
                savePlaylistAndExit()
            }
            .show()
    }

    private fun savePlaylistAndExit() {
        val name = nameEditText.text.toString().trim()
        if (name.isNotEmpty()) {
            createPlaylist()
        } else {
            Toast.makeText(requireContext(), "Создание плейлиста отменено", Toast.LENGTH_SHORT).show()
            navigateBack()
        }
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            val chooser = Intent.createChooser(intent, "Выберите изображение")
            pickImageLauncher.launch(chooser)
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error opening image picker", e)
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                val chooser = Intent.createChooser(intent, "Выберите изображение")
                pickImageLauncher.launch(chooser)
            } catch (e2: Exception) {
                Log.e("ImagePicker", "Alternative method also failed", e2)
            }
        }
    }

    private fun saveImageToPrivateStorage(uri: Uri): String? {
        return try {
            val filePath = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "playlist_covers")
            if (!filePath.exists()) filePath.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "playlist_cover_$timeStamp.jpg"
            val file = File(filePath, fileName)

            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)

            BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }

            inputStream?.close()
            outputStream.close()

            Log.d("SaveImage", "Image saved successfully: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("SaveImage", "Error saving image", e)
            null
        }
    }

    private fun setupFocusListeners() {
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            updateFieldAppearance(nameEditText, nameFrame, nameLabel, hasFocus)
            if (!hasFocus) checkForUnsavedChanges()
        }

        descriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            updateFieldAppearance(descriptionEditText, descriptionFrame, descriptionLabel, hasFocus)
            if (!hasFocus) checkForUnsavedChanges()
        }
    }

    private fun navigateBack() {
        try {
            // Возвращаемся к экрану аудиоплеера
            findNavController().popBackStack()
        } catch (e: Exception) {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupTextWatchers() {
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.toString()?.trim()?.isNotEmpty() == true
                updateFieldAppearance(nameEditText, nameFrame, nameLabel, nameEditText.hasFocus())
                updateCreateButtonState(hasText)
                if (hasText) hasUnsavedChanges = true
            }
        })

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFieldAppearance(descriptionEditText, descriptionFrame, descriptionLabel, descriptionEditText.hasFocus())
                if (s?.toString()?.trim()?.isNotEmpty() == true) hasUnsavedChanges = true
            }
        })
    }

    private fun updateFieldAppearance(editText: EditText, frame: FrameLayout, label: TextView, hasFocus: Boolean) {
        val hasText = editText.text.toString().trim().isNotEmpty()
        val shouldBeBlue = hasFocus

        if (shouldBeBlue) {
            frame.background = blueFrameDrawable
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        } else {
            frame.background = grayFrameDrawable
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }

        if (hasFocus || hasText) {
            label.visibility = View.VISIBLE
            editText.hint = ""
        } else {
            label.visibility = View.GONE
            when (editText.id) {
                R.id.name_playlist -> editText.hint = "Название*"
                R.id.description_playlist -> editText.hint = "Описание"
            }
        }
    }

    private fun updateCreateButtonState(isActive: Boolean) {
        if (isActive) {
            createButton.background = blueButtonDrawable
            createButton.isClickable = true
            createButton.isFocusable = true
            createButton.setOnClickListener { createPlaylist() }
        } else {
            createButton.background = grayButtonDrawable
            createButton.isClickable = false
            createButton.isFocusable = false
            createButton.setOnClickListener(null)
        }
    }

    private fun createPlaylist() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        lifecycleScope.launch {
            try {
                val playlistId = playlistInteractor.createPlaylist(
                    name = name,
                    description = if (description.isNotEmpty()) description else null,
                    coverImagePath = savedImagePath
                )

                if (playlistId > 0) {
                    // Получаем созданный плейлист
                    val createdPlaylist = playlistInteractor.getPlaylistById(playlistId)

                    // Если есть трек, добавляем его в созданный плейлист через ViewModel
                    track?.let { trackToAdd ->
                        createdPlaylist?.let { playlist ->
                            // Используем метод из PlayerViewModel для добавления трека
                            playerViewModel.addTrackToPlaylist(playlist, trackToAdd)

                            // Наблюдаем за статусом добавления
                            playerViewModel.addToPlaylistStatus.collect { status ->
                                when (status) {
                                    is PlayerViewModel.AddToPlaylistStatus.Success -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Плейлист \"$name\" создан и трек добавлен!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        playerViewModel.resetAddToPlaylistStatus()
                                        hasUnsavedChanges = false
                                        navigateBack()
                                    }
                                    is PlayerViewModel.AddToPlaylistStatus.AlreadyExists -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Плейлист \"$name\" создан, но трек уже был добавлен ранее",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        playerViewModel.resetAddToPlaylistStatus()
                                        hasUnsavedChanges = false
                                        navigateBack()
                                    }
                                    is PlayerViewModel.AddToPlaylistStatus.Error -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Плейлист \"$name\" создан, но произошла ошибка при добавлении трека",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        playerViewModel.resetAddToPlaylistStatus()
                                        hasUnsavedChanges = false
                                        navigateBack()
                                    }
                                    else -> {}
                                }
                            }
                        }
                    } ?: run {
                        // Если трека нет, просто сообщаем о создании плейлиста
                        Toast.makeText(requireContext(), "Плейлист \"$name\" создан!", Toast.LENGTH_SHORT).show()
                        hasUnsavedChanges = false
                        navigateBack()
                    }
                } else {
                    Toast.makeText(requireContext(), "Ошибка создания плейлиста", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("NewPlaylistFragment", "Error creating playlist", e)
                Toast.makeText(requireContext(), "Ошибка создания плейлиста", Toast.LENGTH_SHORT).show()
            }
        }
    }
}