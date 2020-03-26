package dajana.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import wang.switchy.hin2n.Application;

public class UriUtils {
    private static final String DAJANA_DIR = "Dajana";

    public static boolean uriIsImage(Uri uri) {
        return FileManager.extensionIsImage(getExtensionFromUri(uri));
    }

    public static String getFullFileName(Uri uri) {
        String extension = getExtensionFromUri(uri);
        String name = getFileName(uri);
        if (name == null) name = UUID.randomUUID().toString();
        else name = name.replace(".", "");
        String fileName = name + "." + extension;
        return fileName;
    }

    public static String getMimeType(Uri uri) {
        String type = Application.getInstance().getContentResolver().getType(uri);
        if (type == null || type.isEmpty()) type = "*/*";
        return type;
    }

    private static String getExtensionFromUri(Uri uri) {
        String mimeType = Application.getInstance().getContentResolver().getType(uri);
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

    private static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = Application.getInstance().getContentResolver()
                    .query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return FileManager.getBaseName(result);
    }

    public static String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = Application.getInstance().getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String copyFileToLocalStorage(Context context, Uri uri) throws IOException {
        String fileName = UriUtils.getFullFileName(uri);
        File folder = new File(getDownloadDirPath());
        if(!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(getDownloadDirPath(),  fileName);

        OutputStream os = null;
        InputStream is = null;

        if (file.exists()) {
            file = new File(getDownloadDirPath(),
                    FileManager.generateUniqueNameForFile(getDownloadDirPath(), fileName));
        }

        if (file.createNewFile()) {
            os = new FileOutputStream(file);
            is = context.getContentResolver().openInputStream(uri);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        }
        return file.getPath();
    }

    private static String getDownloadDirPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + File.separator + DAJANA_DIR;
    }
}
