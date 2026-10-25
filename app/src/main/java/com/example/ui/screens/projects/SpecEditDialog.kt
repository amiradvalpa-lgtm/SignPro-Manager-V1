package com.example.ui.screens.projects

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.entities.ProjectSpecEntity
import com.example.ui.components.common.PersianTextField
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecEditDialog(
    projectId: String,
    spec: ProjectSpecEntity?,
    onDismiss: () -> Unit,
    onSave: (ProjectSpecEntity, Boolean) -> Unit
) {
    val isNew = spec == null

    var subTitle by remember { mutableStateOf(spec?.subTitle ?: "") }
    var signType by remember { mutableStateOf(spec?.signType ?: "چلنیوم طلایی") }
    var lengthStr by remember { mutableStateOf(if (spec != null) spec.lengthCm.toString() else "") }
    var widthStr by remember { mutableStateOf(if (spec != null) spec.widthCm.toString() else "") }
    var depthStr by remember { mutableStateOf(if (spec != null) spec.depthCm.toString() else "0") }
    var description by remember { mutableStateOf(spec?.description ?: "") }

    var attachmentUris by remember {
        mutableStateOf(spec?.attachmentUris?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
    }

    var subTitleError by remember { mutableStateOf<String?>(null) }
    var signTypeError by remember { mutableStateOf<String?>(null) }
    var lengthError by remember { mutableStateOf<String?>(null) }
    var widthError by remember { mutableStateOf<String?>(null) }
    var depthError by remember { mutableStateOf<String?>(null) }

    var signTypeDropdownExpanded by remember { mutableStateOf(false) }

    val signTypePresets = listOf(
        "چلنیوم طلایی (پایه)",
        "چلنیوم الماسی و ستاره‌ای",
        "استیل نقره‌ای و طلایی",
        "حروف پلاستیک (پلکسی ۳ بعدی)",
        "نئون فلکسی سیلیکونی",
        "لایت‌باکس کریستالی / آلومینیومی",
        "کامپوزیت طرح چوب و سنگ",
        "ترمووود فنلاندی",
        "تابلو ال‌ای‌دی ثابت و روان",
        "حروف وکیوم سه بعدی",
        "پیکسل و پوینت لایت فول‌کالر"
    )

    // Android zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUris = attachmentUris.toMutableList()
            uris.forEach { u ->
                val str = u.toString()
                if (!newUris.contains(str)) newUris.add(str)
            }
            attachmentUris = newUris
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "افزودن مشخصات فنی تابلو" else "ویرایش مشخصات تابلو",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PersianTextField(
                    value = subTitle,
                    onValueChange = {
                        subTitle = it
                        subTitleError = null
                    },
                    label = "عنوان زیرمجموعه تابلو *",
                    placeholder = "مثال: حروف اصلی سردر، لوگوی گرد، لایت‌باکس ورودی",
                    isError = subTitleError != null,
                    errorMessage = subTitleError,
                    testTag = "spec_subtitle_input"
                )

                // Sign Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = signTypeDropdownExpanded,
                    onExpandedChange = { signTypeDropdownExpanded = !signTypeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = signType,
                        onValueChange = {
                            signType = it
                            signTypeError = null
                        },
                        label = { Text("نوع تابلو و متریال *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = signTypeDropdownExpanded) },
                        isError = signTypeError != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = signTypeDropdownExpanded,
                        onDismissRequest = { signTypeDropdownExpanded = false }
                    ) {
                        signTypePresets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset) },
                                onClick = {
                                    signType = preset
                                    signTypeError = null
                                    signTypeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (signTypeError != null) {
                    Text(text = signTypeError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Dimensions (Length, Width, Depth) in centimeters
                Text(
                    text = "ابعاد تابلو (بر حسب سانتی‌متر) *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersianTextField(
                        value = lengthStr,
                        onValueChange = {
                            lengthStr = it
                            lengthError = null
                        },
                        label = "طول (cm) *",
                        placeholder = "۳۵۰",
                        keyboardType = KeyboardType.Number,
                        isError = lengthError != null,
                        errorMessage = lengthError,
                        modifier = Modifier.weight(1f),
                        testTag = "spec_length_input"
                    )
                    PersianTextField(
                        value = widthStr,
                        onValueChange = {
                            widthStr = it
                            widthError = null
                        },
                        label = "عرض (cm) *",
                        placeholder = "۱۲۰",
                        keyboardType = KeyboardType.Number,
                        isError = widthError != null,
                        errorMessage = widthError,
                        modifier = Modifier.weight(1f),
                        testTag = "spec_width_input"
                    )
                    PersianTextField(
                        value = depthStr,
                        onValueChange = {
                            depthStr = it
                            depthError = null
                        },
                        label = "عمق (cm) *",
                        placeholder = "۸",
                        keyboardType = KeyboardType.Number,
                        isError = depthError != null,
                        errorMessage = depthError,
                        modifier = Modifier.weight(1f),
                        testTag = "spec_depth_input"
                    )
                }

                PersianTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "توضیحات فنی، رنگ نور و جنس لبه",
                    placeholder = "مثال: پلکسی یرلانگ تایوان، SMD بلوکی اینجکشن لنزدار با ترانس ۳۳ آمپر",
                    singleLine = false,
                    testTag = "spec_description_input"
                )

                // Attachments from phone
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فایل‌ها و تصاویر طرح از گوشی",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("انتخاب عکس")
                        }
                    }

                    if (attachmentUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(attachmentUris) { uriStr ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uriStr),
                                        contentDescription = "طرح ضمیمه",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = {
                                            attachmentUris = attachmentUris.filter { it != uriStr }
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "حذف عکس",
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (subTitle.trim().isEmpty()) {
                        subTitleError = "عنوان زیرمجموعه الزامی است."
                        hasError = true
                    }
                    if (signType.trim().isEmpty()) {
                        signTypeError = "نوع تابلو الزامی است."
                        hasError = true
                    }
                    val l = lengthStr.toDoubleOrNull()
                    if (l == null || l <= 0) {
                        lengthError = "طول نامعتبر است."
                        hasError = true
                    }
                    val w = widthStr.toDoubleOrNull()
                    if (w == null || w <= 0) {
                        widthError = "عرض نامعتبر است."
                        hasError = true
                    }
                    val d = depthStr.toDoubleOrNull() ?: 0.0

                    if (!hasError) {
                        val entity = spec?.copy(
                            subTitle = subTitle.trim(),
                            signType = signType.trim(),
                            lengthCm = l ?: 0.0,
                            widthCm = w ?: 0.0,
                            depthCm = d,
                            attachmentUris = attachmentUris.joinToString(","),
                            description = description.trim()
                        ) ?: ProjectSpecEntity(
                            id = UUID.randomUUID().toString(),
                            projectId = projectId,
                            subTitle = subTitle.trim(),
                            signType = signType.trim(),
                            lengthCm = l ?: 0.0,
                            widthCm = w ?: 0.0,
                            depthCm = d,
                            attachmentUris = attachmentUris.joinToString(","),
                            description = description.trim()
                        )
                        onSave(entity, isNew)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_spec_btn")
            ) {
                Text("ثبت مشخصات تابلو", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
