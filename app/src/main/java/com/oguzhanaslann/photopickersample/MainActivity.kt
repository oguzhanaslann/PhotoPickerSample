package com.oguzhanaslann.photopickersample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun MainScreen() {

    val selectedUri = remember { mutableStateOf<Uri?>(null) }
    val selectedUriAsString = remember(selectedUri) {
        derivedStateOf {
            selectedUri.value?.toString() ?: ""
        }
    }
    val lastPickPlace = remember { mutableStateOf<LastPickPlace?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        Log.d(TAG, "MainScreen:photoPicker uri : $uri")
        uri?.let {
            selectedUri.value = it
            lastPickPlace.value = LastPickPlace.PHOTO_PICKER
        }
    }

    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 2)
    ) {
        Log.d(TAG, "MainScreen:multiplePhotoPicker uris :  $it")
        it.firstOrNull()?.let {
            selectedUri.value = it
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
        selectedUri.value = it
        lastPickPlace.value = LastPickPlace.MEDIA_STORE
    }

    val documentTreePicker = rememberLauncherForActivityResult(
        contract = PickContentLegacyDocumentTree()
    ) {
        Log.d(TAG, "MainScreen:documentTreePicker uri :  $it")
        selectedUri.value = it
        lastPickPlace.value = LastPickPlace.DOCUMENT_TREE
    }

    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        SelectedImageView(selectedUriAsString, lastPickPlace)

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
        PhotoPickerSection(isPhotoPickerAvailable, photoPicker, multiplePhotoPicker, selectedUri.value)
    }
}

@Composable
private fun SelectedImageView(
    selectedUri: State<String>,
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
    multiplePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    selectedUri: Uri?
) {
    val context = LocalContext.current


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

    Spacer(modifier = Modifier.height(8.dp))

    AnimatedVisibility(visible = selectedUri != null  ) {
        Button(onClick = {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(selectedUri!!, flag)
            Toast.makeText(context, "Uri permission granted", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "take uri permission for longer use")
        }
    }
}
