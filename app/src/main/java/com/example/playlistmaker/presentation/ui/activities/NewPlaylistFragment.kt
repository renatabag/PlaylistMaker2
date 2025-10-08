package com.example.playlistmaker.presentation.ui.fragments

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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.playlistmaker.R
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.NewPlaylistViewModel
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
    private lateinit var titleText: TextView
    private lateinit var createButtonText: TextView
    private lateinit var blueButtonDrawable: Drawable
    private lateinit var grayButtonDrawable: Drawable
    private lateinit var blueFrameDrawable: Drawable
    private lateinit var grayFrameDrawable: Drawable

    private val playlistInteractor: PlaylistInteractor by inject()
    private val viewModel: NewPlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var savedImagePath: String? = null
    private var track: TrackUi? = null
    private var isEditMode = false
    private var currentPlaylistId: Long = -1L
    private var hasUnsavedChanges: Boolean = false

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем параметры
        track = arguments?.getParcelable(ARG_TRACK)
        isEditMode = arguments?.getBoolean(ARG_IS_EDIT_MODE, false) ?: false
        currentPlaylistId = arguments?.getLong(ARG_PLAYLIST_ID, -1L) ?: -1L

        // Восстановление состояния
        savedInstanceState?.let {
            savedImagePath = it.getString("saved_image_path")
            hasUnsavedChanges = it.getBoolean("has_unsaved_changes", false)
        }

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleImageSelection(uri)
                }
            }
        }
    }

    companion object {
        private const val ARG_TRACK = "track"
        private const val ARG_PLAYLIST_ID = "playlist_id"
        private const val ARG_IS_EDIT_MODE = "is_edit_mode"

        // Для создания плейлиста (из медиатеки или с треком)
        fun newInstance(track: TrackUi? = null): NewPlaylistFragment {
            return NewPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TRACK, track)
                    putBoolean(ARG_IS_EDIT_MODE, false)
                }
            }
        }

        // Для редактирования существующего плейлиста
        fun newEditInstance(playlistId: Long): NewPlaylistFragment {
            return NewPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_PLAYLIST_ID, playlistId)
                    putBoolean(ARG_IS_EDIT_MODE, true)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.new_playlist, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("playlist_name", nameEditText.text.toString())
        outState.putString("playlist_description", descriptionEditText.text.toString())
        outState.putString("saved_image_path", savedImagePath)
        outState.putBoolean("has_unsaved_changes", hasUnsavedChanges)
        selectedImageUri?.let { uri ->
            outState.putString("selected_image_uri", uri.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация Drawable
        blueButtonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.blue_frame)!!
        grayButtonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gray_frame)!!
        blueFrameDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.frame_border_blue)!!
        grayFrameDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.frame_border_grey)!!

        // Инициализация View
        createButton = view.findViewById(R.id.create_playlist_button)
        nameEditText = view.findViewById(R.id.name_playlist)
        descriptionEditText = view.findViewById(R.id.description_playlist)
        nameFrame = view.findViewById(R.id.name_frame)
        descriptionFrame = view.findViewById(R.id.description_frame)
        nameLabel = view.findViewById(R.id.name_label)
        descriptionLabel = view.findViewById(R.id.description_label)
        coverImageButton = view.findViewById(R.id.add_picture)
        titleText = view.findViewById(R.id.title_text)
        createButtonText = view.findViewById(R.id.create_button_text)

        // Настройка UI в зависимости от режима
        setupUI()

        // Восстановление состояния
        savedInstanceState?.let { bundle ->
            bundle.getString("playlist_name")?.let { name ->
                nameEditText.setText(name)
            }
            bundle.getString("playlist_description")?.let { description ->
                descriptionEditText.setText(description)
            }
            savedImagePath = bundle.getString("saved_image_path")
            hasUnsavedChanges = bundle.getBoolean("has_unsaved_changes", false)

            // Восстанавливаем изображение
            savedImagePath?.let { path ->
                val imageFile = File(path)
                if (imageFile.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(path)
                        coverImageButton.setImageBitmap(bitmap)
                        coverImageButton.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    } catch (e: Exception) {
                        Log.e("NewPlaylistFragment", "Error restoring image", e)
                        coverImageButton.setImageResource(R.drawable.add_photo)
                    }
                }
            }

            bundle.getString("selected_image_uri")?.let { uriString ->
                selectedImageUri = Uri.parse(uriString)
            }
        }

        // Если режим редактирования - загружаем данные плейлиста
        if (isEditMode && currentPlaylistId != -1L) {
            viewModel.loadPlaylistForEditing(currentPlaylistId)
        }

        setupClickListeners()
        setupObservers()
        setupFocusListeners()
        setupTextWatchers()

        // Обновляем состояние кнопки после восстановления
        val hasName = nameEditText.text.toString().trim().isNotEmpty()
        updateCreateButtonState(hasName)

        // Обновляем внешний вид полей
        updateFieldAppearance(nameEditText, nameFrame, nameLabel, nameEditText.hasFocus())
        updateFieldAppearance(descriptionEditText, descriptionFrame, descriptionLabel, descriptionEditText.hasFocus())
    }

    private fun setupUI() {
        if (isEditMode) {
            titleText.text = getString(R.string.edit_playlist)
            createButtonText.text = getString(R.string.save)
            // В режиме редактирования кнопка изначально активна
            updateCreateButtonState(true)
        } else {
            titleText.text = getString(R.string.new_playlist)
            createButtonText.text = getString(R.string.create)
            // В режиме создания кнопка изначально неактивна
            updateCreateButtonState(false)
        }
    }

    private fun setupClickListeners() {
        view?.findViewById<ImageButton>(R.id.menu_button)?.setOnClickListener {
            handleBackNavigation()
        }

        coverImageButton.setOnClickListener {
            openImagePicker()
        }

        // Обработка кнопки "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistForEditing.collect { playlist ->
                playlist?.let {
                    populatePlaylistData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.creationSuccess.collect { success ->
                if (success) {
                    handleSuccess()
                }
            }
        }
    }

    private fun handleBackNavigation() {
        checkForUnsavedChanges()
        if (hasUnsavedChanges && !isEditMode) {
            showExitConfirmationDialog()
        } else {
            navigateBack()
        }
        // В режиме редактирования при нажатии назад просто закрываем без подтверждения
    }

    private fun populatePlaylistData(playlist: com.example.playlistmaker.domain.models.Playlist) {
        nameEditText.setText(playlist.name)
        descriptionEditText.setText(playlist.description ?: "")

        // Загружаем изображение обложки если есть
        playlist.coverImagePath?.let { path ->
            val imageFile = File(path)
            if (imageFile.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    coverImageButton.setImageBitmap(bitmap)
                    coverImageButton.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    savedImagePath = path
                } catch (e: Exception) {
                    Log.e("NewPlaylistFragment", "Error loading playlist image", e)
                }
            }
        }

        updateCreateButtonState(true)
        hasUnsavedChanges = false // Сбрасываем флаг после загрузки данных
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохраненные данные будут потеряны")
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Завершить") { dialog, _ ->
                dialog.dismiss()
                discardChangesAndExit()
            }
            .show()
    }

    private fun discardChangesAndExit() {
        // Сбрасываем все поля
        nameEditText.text.clear()
        descriptionEditText.text.clear()
        coverImageButton.setImageResource(R.drawable.add_photo)
        coverImageButton.scaleType = android.widget.ImageView.ScaleType.CENTER
        selectedImageUri = null
        savedImagePath = null

        // Сбрасываем состояние кнопки
        updateCreateButtonState(false)

        // Сбрасываем внешний вид полей
        updateFieldAppearance(nameEditText, nameFrame, nameLabel, false)
        updateFieldAppearance(descriptionEditText, descriptionFrame, descriptionLabel, false)

        // Сбрасываем флаг несохраненных изменений
        hasUnsavedChanges = false

        // Переходим к фрагменту плейлистов
        navigateToPlaylists()
    }

    private fun navigateBack() {
        try {
            findNavController().navigateUp()
        } catch (e: Exception) {
            Log.e("NewPlaylistFragment", "Error navigating back", e)
            parentFragmentManager.popBackStack()
        }
    }

    private fun navigateToPlaylists() {
        try {
            findNavController().navigate(R.id.fragment_list)
        } catch (e: Exception) {
            Log.e("NewPlaylistFragment", "Error navigating to playlists", e)
            parentFragmentManager.popBackStack()
        }
    }

    // Остальные методы (handleImageSelection, openImagePicker, saveImageToPrivateStorage,
    // setupFocusListeners, setupTextWatchers, updateFieldAppearance, updateCreateButtonState)
    // остаются такими же как в вашем исходном коде

    private fun handleImageSelection(uri: Uri) {
        coverImageButton.setImageURI(uri)
        coverImageButton.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        selectedImageUri = uri
        savedImagePath = saveImageToPrivateStorage(uri)
        hasUnsavedChanges = true
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

    private fun setupTextWatchers() {
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.toString()?.trim()?.isNotEmpty() == true
                updateFieldAppearance(nameEditText, nameFrame, nameLabel, nameEditText.hasFocus())
                updateCreateButtonState(hasText)
                if (hasText) hasUnsavedChanges = true

                // В режиме редактирования проверяем изменения
                if (isEditMode) {
                    checkForChangesInEditMode()
                }
            }
        })

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFieldAppearance(descriptionEditText, descriptionFrame, descriptionLabel, descriptionEditText.hasFocus())
                if (s?.toString()?.trim()?.isNotEmpty() == true) hasUnsavedChanges = true

                // В режиме редактирования проверяем изменения
                if (isEditMode) {
                    checkForChangesInEditMode()
                }
            }
        })
    }

    private fun checkForChangesInEditMode() {
        val currentName = nameEditText.text.toString().trim()
        val currentDescription = descriptionEditText.text.toString().trim()

        val originalPlaylist = viewModel.playlistForEditing.value
        if (originalPlaylist != null) {
            val nameChanged = currentName != originalPlaylist.name
            val descriptionChanged = currentDescription != (originalPlaylist.description ?: "")
            val hasChanges = nameChanged || descriptionChanged || (savedImagePath != originalPlaylist.coverImagePath)

            // Обновляем текст кнопки, если есть изменения
            if (hasChanges && currentName.isNotEmpty()) {
                createButtonText.text = getString(R.string.save)
                updateCreateButtonState(true)
            } else if (currentName.isEmpty()) {
                updateCreateButtonState(false)
            }
        }
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
            createButton.setOnClickListener {
                if (isEditMode) {
                    savePlaylistChanges()
                } else {
                    createNewPlaylist()
                }
            }
        } else {
            createButton.background = grayButtonDrawable
            createButton.isClickable = false
            createButton.isFocusable = false
            createButton.setOnClickListener(null)
        }
    }

    private fun createNewPlaylist() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (name.isNotEmpty()) {
            viewModel.createPlaylist(name, if (description.isEmpty()) null else description, savedImagePath)
        }
    }

    private fun savePlaylistChanges() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (name.isNotEmpty() && currentPlaylistId != -1L) {
            viewModel.updatePlaylist(currentPlaylistId, name, if (description.isEmpty()) null else description, savedImagePath)
        }
    }

    private fun checkForUnsavedChanges() {
        val hasName = nameEditText.text.toString().trim().isNotEmpty()
        val hasDescription = descriptionEditText.text.toString().trim().isNotEmpty()
        val hasImage = savedImagePath != null
        hasUnsavedChanges = hasName || hasDescription || hasImage
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
    private fun handleSuccess() {
        track?.let { trackToAdd ->
            viewModel.addTrackToCreatedPlaylist(trackToAdd)
        }

        if (isEditMode) {
            Toast.makeText(
                requireContext(),
                "Плейлист успешно обновлен",
                Toast.LENGTH_SHORT
            ).show()

            returnToPlaylistFragment()
        } else {
            Toast.makeText(
                requireContext(),
                "Плейлист успешно создан",
                Toast.LENGTH_SHORT
            ).show()

            navigateToPlaylists()
        }
    }

    private fun returnToPlaylistFragment() {
        try {
            findNavController().navigateUp()
        } catch (e: Exception) {
            Log.e("NewPlaylistFragment", "Error returning to playlist fragment", e)
            parentFragmentManager.popBackStack()
        }
    }

}