package com.oguzhanaslann.photopickersample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract

/**
 *  Caution : requires READ_EXTERNAL_STORAGE permission in manifest file to work when using api 29 and below
 *      for api 30, android 11 : READ_EXTERNAL_STORAGE permission is required
 *      for api 29, android 10 : one of READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE permissions is required
 *      for api 28 and below :  READ_EXTERNAL_STORAGE permission is required
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
