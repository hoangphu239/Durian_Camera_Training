package com.netsservices.dct.presentation.home.components

import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.home.HomeViewModel

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun PreviewImageDialog(
    viewModel: HomeViewModel,
    previewImage: ByteArray
) {
    val bitmap = remember(previewImage) {
        BitmapFactory.decodeByteArray(previewImage, 0, previewImage.size)
    }

    val scale = remember { mutableFloatStateOf(1f) }
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale.floatValue = (scale.floatValue * zoomChange).coerceIn(1f, 5f)
    }


    AlertDialog(
        onDismissRequest = {},
        title = {
            AppText(
                text = stringResource(R.string.preview_image),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(bitmap.width.toFloat() / bitmap.height)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.floatValue
                            scaleY = scale.floatValue
                        }
                        .transformable(transformableState)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.onPreviewAccept() }) {
                AppText(
                    text = stringResource(R.string.accept),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onPreviewCancel() }) {
                AppText(
                    text = stringResource(R.string.cancel),
                    fontSize = 14.sp,
                    color = R.color.gray
                )
            }
        }
    )
}