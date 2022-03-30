package com.sms.moLotus.feature

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log


object FileUtils {


    fun getUriRealPath(ctx: Context, uri: Uri): String? {
        var ret: String? = ""
        ret = getUriRealPathAboveKitkat(ctx, uri)

        Log.e("======", "getUriRealPath ::: $ret")
        return ret
    }

    /*
This method will parse out the real local file path from the file content URI.
The method is only applied to the android SDK version number that is bigger than 19.
*/
    private fun getUriRealPathAboveKitkat(ctx: Context?, uri: Uri?): String? {
        var ret: String? = ""
        if (ctx != null && uri != null) {
            if (isContentUri(uri)) {
                ret = when {
                    isGooglePhotoDoc(uri.authority) -> {
                        uri.lastPathSegment
                    }

                    else -> {
                        getImageRealPath(ctx.contentResolver, uri, null)
                    }
                }
            } else if (isMIUIDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    ret = Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }
                ret
            } else if (isFileUri(uri)) {
                ret = uri.path
            } else if (isDocumentUri(ctx, uri)) {
                // Get uri related document id.
                val documentId = DocumentsContract.getDocumentId(uri)
                // Get uri authority.
                val uriAuthority = uri.authority
                if (isMediaDoc(uriAuthority)) {
                    val idArr = documentId.split(":".toRegex()).toTypedArray()
                    if (idArr.size == 2) {
                        // First item is document type.
                        val docType = idArr[0]
                        // Second item is document real id.
                        val realDocId = idArr[1]
                        // Get content uri by document type.
                        var mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        if ("image" == docType) {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == docType) {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == docType) {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        // Get where clause with real document id.
                        val whereClause = MediaStore.Images.Media._ID + " = " + realDocId
                        ret =
                            getImageRealPath(ctx.contentResolver, mediaContentUri, whereClause)
                    }
                } else if (isDownloadDoc(uriAuthority)) {
                    // Build download URI.
                    val downloadUri = Uri.parse("content://downloads/public_downloads")
                    // Append download document id at URI end.
                    val downloadUriAppendId =
                        ContentUris.withAppendedId(downloadUri, java.lang.Long.valueOf(documentId))
                    ret = getImageRealPath(ctx.contentResolver, downloadUriAppendId, null)
                } else if (isExternalStoreDoc(uriAuthority)) {
                    val idArr = documentId.split(":".toRegex()).toTypedArray()
                    if (idArr.size == 2) {
                        val type = idArr[0]
                        val realDocId = idArr[1]
                        if ("primary".equals(type, ignoreCase = true)) {
                            ret = Environment.getExternalStorageDirectory()
                                .toString() + "/" + realDocId
                        }
                    }
                }
            }
        }
        return ret
    }

    fun isMIUIDocument(uri: Uri): Boolean {
        return "com.mi.android.globalFileexplorer.myprovider" == uri.authority
    }

    /* Check whether the current android os version is bigger than KitKat or not. */
    private fun isAboveKitKat(): Boolean {
        var ret = false
        ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        return ret
    }

    /* Check whether this uri represent a document or not. */
    private fun isDocumentUri(ctx: Context?, uri: Uri?): Boolean {
        var ret = false
        if (ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri)
        }
        return ret
    }

    /* Check whether this URI is a content URI or not.
*  content uri like content://media/external/images/media/1302716
*  */
    private fun isContentUri(uri: Uri?): Boolean {
        var ret = false
        if (uri != null) {
            val uriSchema = uri.scheme
            if ("content".equals(uriSchema, ignoreCase = true)) {
                ret = true
            }
        }
        return ret
    }

    /* Check whether this URI is a file URI or not.
*  file URI like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
* */
    private fun isFileUri(uri: Uri?): Boolean {
        var ret = false
        if (uri != null) {
            val uriSchema = uri.scheme
            if ("file".equals(uriSchema, ignoreCase = true)) {
                ret = true
            }
        }
        return ret
    }

    /* Check whether this document is provided by ExternalStorageProvider. Return true means the file is saved in external storage. */
    private fun isExternalStoreDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.externalstorage.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Check whether this document is provided by DownloadsProvider. return true means this file is a downloaded file. */
    private fun isDownloadDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.providers.downloads.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /*
Check if MediaProvider provides this document, if true means this image is created in the android media app.
*/
    private fun isMediaDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.providers.media.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /*
Check whether google photos provide this document, if true means this image is created in the google photos app.
*/
    private fun isGooglePhotoDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.google.android.apps.photos.content" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Return uri represented document file real local path.*/
    private fun getImageRealPath(
        contentResolver: ContentResolver,
        uri: Uri,
        whereClause: String?
    ): String? {
        var ret = ""
        // Query the URI with the condition.
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(
            column
        )
        val cursor: Cursor? = contentResolver.query(uri, projection, whereClause, null, null)

        if (cursor != null) {
            val moveToFirst = cursor.moveToFirst()
            if (moveToFirst) {
                // Get columns name by URI type.
                var columnName = MediaStore.Images.Media.DATA
                when {
                    uri === MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Images.Media.DATA
                    }
                    uri === MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) -> {
                        columnName = MediaStore.Images.Media.DATA
                    }
                    uri === MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) -> {
                        columnName = MediaStore.Audio.Media.DATA
                    }
                    uri === MediaStore.Audio.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Audio.Media.DATA
                    }
                    uri === MediaStore.Video.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Video.Media.DATA
                    }
                }
                // Get column index.
                val imageColumnIndex = cursor.getColumnIndex(columnName)
                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex)
            }
        }
        return ret
    }


    /*@Throws(Exception::class)
    fun getFileFromUri(context: Context, uri: Uri): File {
        var path: String? = null

        // DocumentProvider
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(
                    context,
                    uri
                )
            ) { // TODO: 2015. 11. 17. KITKAT


                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory()
                            .toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isMIUIDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory()
                            .toString() + "/" + split[1]
                    }

                } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    path =
                        getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) { // MediaProvider
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    path = getDataColumn(
                        context,
                        contentUri,
                        selection,
                        selectionArgs
                    )
                } else if (isGoogleDrive(uri)) { // Google Drive
                    val TAG = "isGoogleDrive"
                    path = TAG
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(";".toRegex()).toTypedArray()
                    val acc = split[0]
                    val doc = split[1]

                    *//*
                     * @details google drive document data. - acc , docId.
                     * *//*return saveFileIntoExternalStorageByUri(
                        context,
                        uri
                    )
                } // MediaStore (and general)
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                path = getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                path = uri.path
            }
            File(path)
        } else {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            File(cursor?.getString(cursor.getColumnIndex("_data")))
        }
    }

    */
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is GoogleDrive.
     *//*
    fun isGoogleDrive(uri: Uri): Boolean {
        return uri.authority.equals("com.google.android.apps.docs.storage", ignoreCase = true)
    }

    */
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     *//*
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    */
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     *//*
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    */
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     *//*
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    */
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     *//*
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isMIUIDocument(uri: Uri): Boolean {
        return "com.mi.android.globalFileexplorer.myprovider" == uri.authority
    }

    private fun makeEmptyFileIntoExternalStorageWithTitle(title: String?): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root, title)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    @Throws(Exception::class)
    fun saveBitmapFileIntoExternalStorageWithTitle(bitmap: Bitmap, title: String) {
        val fileOutputStream = FileOutputStream(
            makeEmptyFileIntoExternalStorageWithTitle(
                "$title.png"
            )
        )
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
    }

    @Throws(Exception::class)
    fun saveFileIntoExternalStorageByUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalSize = inputStream!!.available()
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        val fileName = getFileName(context, uri)
        val file = makeEmptyFileIntoExternalStorageWithTitle(fileName)
        bis = BufferedInputStream(inputStream)
        bos = BufferedOutputStream(
            FileOutputStream(
                file, false
            )
        )
        val buf = ByteArray(originalSize)
        bis.read(buf)
        do {
            bos.write(buf)
        } while (bis.read(buf) != -1)
        bos.flush()
        bos.close()
        bis.close()
        return file
    }*/
}