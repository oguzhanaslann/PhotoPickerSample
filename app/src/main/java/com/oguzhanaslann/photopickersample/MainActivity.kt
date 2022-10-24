package com.oguzhanaslann.photopickersample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.oguzhanaslann.photopickersample.ui.theme.PhotoPickerSampleTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoPickerSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

/**
 *  Caution : requires READ_EXTERNAL_STORAGE permission in manifest file to work when using api 29 and below
 */
class PickContentLegacyMediaStore : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == ComponentActivity.RESULT_OK) {
            val data = intent?.data
            return data
        }

        return null
    }
}

/**
 *  No permission needed since using StorageAccessFramework
 *  @see [https://developer.android.com/training/data-storage/shared/documents-files]
 */
class PickContentLegacyDocumentTree : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == ComponentActivity.RESULT_OK) {
            val data = intent?.data
            return data
        }

        return null
    }
}

enum class LastPickPlace {
    MEDIA_STORE,
    DOCUMENT_TREE,
    PHOTO_PICKER
}

@Composable
fun MainScreen() {

    val selectedUri = remember { mutableStateOf("") }
    val lastPickPlace = remember { mutableStateOf<LastPickPlace?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        Log.d(TAG, "MainScreen:photoPicker uri : $uri")
        uri?.let {
            selectedUri.value = it.toString()
            lastPickPlace.value = LastPickPlace.PHOTO_PICKER
        }
    }

    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 2)
    ) {
        Log.d(TAG, "MainScreen:multiplePhotoPicker uris :  $it")
        it.firstOrNull()?.let {
            selectedUri.value = it.toString()
            lastPickPlace.value = LastPickPlace.PHOTO_PICKER
        }
    }

    val isPhotoPickerAvailable = remember {
        ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = PickContentLegacyMediaStore()
    ) {
        Log.d(TAG, "MainScreen:mediaPicker uri :  $it")
        selectedUri.value = it.toString()
        lastPickPlace.value = LastPickPlace.MEDIA_STORE
    }

    val documentTreePicker = rememberLauncherForActivityResult(
        contract = PickContentLegacyDocumentTree()
    ) {
        Log.d(TAG, "MainScreen:documentTreePicker uri :  $it")
        selectedUri.value = it.toString()
        lastPickPlace.value = LastPickPlace.DOCUMENT_TREE
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        SelectedImageView(selectedUri, lastPickPlace)

        Button(onClick = {
            mediaPicker.launch(Unit)
        }) {
            Text(text = "Pick image with MediaStore")
        }

        Button(onClick = {
            documentTreePicker.launch(Unit)
        }) {
            Text(text = "Pick image with Document picker")
        }

        Spacer(modifier = Modifier.height(16.dp))
        PhotoPickerSection(isPhotoPickerAvailable, photoPicker, multiplePhotoPicker)
    }
}

@Composable
private fun SelectedImageView(
    selectedUri: MutableState<String>,
    lastPickPlace: MutableState<LastPickPlace?>
) {
    if (selectedUri.value.isNotEmpty()) {
        Text(
            text = "Last Selection was from ${lastPickPlace.value}",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        SubcomposeAsyncImage(
            model = selectedUri.value,
            contentDescription = null,
            loading = {
                Surface(
                    modifier = Modifier.size(48.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                    )
                }
            },
            success = {
                Surface(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            },

            modifier = Modifier
                .padding(16.dp)
                .size(256.dp),
        )
    } else {
        Surface(
            color = Color.Gray,
            modifier = Modifier
                .padding(16.dp)
                .size(256.dp),
            shape = RoundedCornerShape(16.dp),
            content = {}
        )
    }
}

@Composable
private fun PhotoPickerSection(
    isPhotoPickerAvailable: Boolean,
    photoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    multiplePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>
) {
    Text(text = "isPhotoPickerAvailable: $isPhotoPickerAvailable")
    Button(
        enabled = isPhotoPickerAvailable,
        onClick = {
            photoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) {
        Text(text = "Pick image with Photo picker")
    }

    Button(
        enabled = isPhotoPickerAvailable,
        onClick = {
            multiplePhotoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) {
        Text(text = "Pick multi image with Photo picker")
    }

    if (!isPhotoPickerAvailable) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "Photo picker is not available on this device, api will use document picker",
            textAlign = TextAlign.Center
        )

        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 24.dp),
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }) {
            Text(
                text = "Pick image with Photo picker ignore availability check",
                textAlign = TextAlign.Center
            )
        }
    }
}
