package com.tencent.testglsurface;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 与文件相关的处理工具类
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    private static final String PIC_POSTFIX_JPEG = ".jpg";
    private static final String PIC_POSTFIX_PNG = ".png";
    private static final String PIC_POSTFIX_WEBP = ".webp";


    public static boolean zip(File src, File dest) {
        return zip(new File[]{src}, dest);
    }

    public static boolean zip(File[] srcFiles, File dest) {
        if (srcFiles != null && srcFiles.length >= 1 && dest != null) {
            boolean resu = false;
            ZipOutputStream zos = null;

            try {
                byte[] e = new byte[4096];
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest, false)));
                File[] arr$ = srcFiles;
                int len$ = srcFiles.length;

                for (int i$ = 0; i$ < len$; ++i$) {
                    File src = arr$[i$];
                    doZip(zos, src, (String) null, e);
                }
                zos.flush();
                zos.closeEntry();
                resu = true;
            } catch (IOException var12) {
                resu = false;
            } finally {
                try {
                    if (zos != null) {
                        zos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return resu;
        } else {
            return false;
        }
    }

    public static boolean writeFile(String path, byte[] data) {
        boolean result = true;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
            result = false;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close file after write", e);
            }
        }
        return result;
    }

    private static void doZip(ZipOutputStream zos, File file, String root, byte[] buffer) throws IOException {
        if (zos != null && file != null) {
            if (!file.exists()) {
                throw new FileNotFoundException("Target File is missing");
            } else {
                BufferedInputStream bis = null;
                boolean readLen = false;
                String rootName = TextUtils.isEmpty(root) ? file.getName() : root + File.separator + file.getName();
                if (file.isFile()) {
                    try {
                        bis = new BufferedInputStream(new FileInputStream(file));
                        zos.putNextEntry(new ZipEntry(rootName));

                        int var13;
                        while (-1 != (var13 = bis.read(buffer, 0, buffer.length))) {
                            zos.write(buffer, 0, var13);
                        }

                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException var12) {
                        try {
                            if (bis != null) {
                                bis.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        throw var12;
                    }
                } else if (file.isDirectory()) {
                    File[] subFiles = file.listFiles();
                    File[] arr$ = subFiles;
                    if (subFiles != null) {
                        int len$ = subFiles.length;
                        for (int i$ = 0; i$ < len$; ++i$) {
                            File subFile = arr$[i$];
                            doZip(zos, subFile, rootName, buffer);
                        }
                    }
                }
            }
        } else {
            throw new IOException("I/O Object got NullPointerException");
        }
    }


    public static String checkPhoto(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        if (new File(path).exists()) {
            return path;
        }
        int slashIndex = path.lastIndexOf("/");
        String lastStr = path.substring(slashIndex);
        int dotIndex = lastStr.lastIndexOf(".");
        if (dotIndex == -1) {
            String jpeg = path + PIC_POSTFIX_JPEG;
            if (new File(jpeg).exists()) {
                return jpeg;
            }
            String png = path + PIC_POSTFIX_PNG;
            if (new File(png).exists()) {
                return png;
            }
        }
        return path;
    }

    public static String loadAssetsString(Context context, String path) {
        StringBuilder buf = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                buf.append(line);
                buf.append("\n");
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return buf.toString();
        }
    }

    public static String checkAssetsPhoto(Context context, String path) {
        if (TextUtils.isEmpty(path)) return null;
        AssetManager assets = context.getAssets();

        InputStream stream = null;
        try {
            stream = assets.open(path);
            return path;
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(stream);
        }

        if (path.lastIndexOf(".") != -1) {
            String webp = path.substring(0, path.lastIndexOf('.') + 1) + "webp";
            try {
                stream = assets.open(webp);
                return webp;
            } catch (IOException e) {
            } finally {
                IOUtils.closeQuietly(stream);
            }
            return null;
        }

        String jpg = path + PIC_POSTFIX_JPEG;
        try {
            stream = assets.open(jpg);
            return jpg;
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(stream);
        }

        String webp = path + PIC_POSTFIX_WEBP;
        try {
            stream = assets.open(webp);
            return webp;
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(stream);
        }

        String png = path + PIC_POSTFIX_PNG;
        try {
            stream = assets.open(png);
            return png;
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(stream);
        }

        return null;
    }

    public static boolean exists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        // assets中的文件，默认一定存在；非assets中的文件，需要正常判断是否存在
        if (path.indexOf("assets") >= 0 || new File(path).exists()) {
            return true;
        }

        return false;
    }

    public static boolean copyFile(String srcPath, String dstPath) {
        return copyFile(new File(srcPath), new File(dstPath));
    }

    public static boolean copyFile(File srcFile, File dstFile) {
        InputStream fosfrom = null;
        OutputStream fosto = null;
        try {
            if (!srcFile.exists()) {
                return false;
            }
            fosfrom = new FileInputStream(srcFile);
            fosto = new FileOutputStream(dstFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "", ex);
        } finally {
            IOUtils.closeQuietly(fosfrom);
            IOUtils.closeQuietly(fosto);
        }
        return false;
    }

    public static boolean copyFile(InputStream is, OutputStream os) {
        if (is == null || os == null) return false;
        try {
            byte[] bs = new byte[1024];
            int len;
            while ((len = is.read(bs)) > 0) {
                os.write(bs, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return true;
    }

    public static boolean copyFile(InputStream fosFrom, String dstPath) {
        OutputStream fosTo = null;
        try {
            fosTo = new FileOutputStream(dstPath);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosFrom.read(bt)) > 0) {
                fosTo.write(bt, 0, c);
            }
            return true;
        } catch (Exception ex) {

        } finally {
            IOUtils.closeQuietly(fosFrom);
            IOUtils.closeQuietly(fosTo);
        }
        return false;
    }

    public static void asyncCopyFile(InputStream srcInputStream, String dstPath, OnFileCopyListener listener) {
        FileCopyTask task = new FileCopyTask(srcInputStream, dstPath);
        task.setOnFileCopyListener(listener);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static boolean Move(File srcFile, String destPath) {
        // Destination directory
        File dir = new File(destPath);

        // Move file to new directory
        boolean success = srcFile.renameTo(new File(dir, srcFile.getName()));

        return success;
    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件地址
     * @param newPath 新的文件地址
     */
    public static boolean rename(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        boolean success = oleFile.renameTo(newFile);
        return success;
    }

    public static void delete(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File f = new File(path);
        delete(f);
    }

    //递归删除文件及文件夹
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    public static String getFileNameFromUrl(String url) {
        if (url == null)
            return null;
//        Log.v(TAG, "getFileNameFromUrl, url = %s", url);
        int index = url.lastIndexOf("/");
//        Log.v(TAG, "index of / is %d", index);
        if (index == -1) {
            return null;
        }
        // e.g. http://adb.abc/asdf/
        if (index == url.length() - 1) {
            return null;
        }
        String fileName = url.substring(index + 1);
//        Log.v(TAG, "fileName is %s", fileName);
        return fileName;
    }

    public static String load(File file) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            stream.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (FileNotFoundException e) {
            // ignore
        } catch (Exception e) {

            Log.e(TAG, "", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return null;
    }

    public static void save(File file, String text) {
        OutputStream stream = null;
        BufferedWriter writer = null;
        try {
            stream = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
            writer.write(text);
        } catch (Exception e) {

            Log.e(TAG, "", e);
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(writer);
        }
    }


    /**
     * zip解包工具
     *
     * @param zipFile   zip包路径
     * @param targetDir 解压路径
     * @return 返回主文件夹的名称
     */
    public synchronized static String unZip(String zipFile, String targetDir) {
        if (TextUtils.isEmpty(zipFile)) {
            return null;
        }

        // 文件不存在
        File file = new File(zipFile);
        if (!file.exists()) {
            return null;
        }
        File targetFolder = new File(targetDir);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        String dataDir = null;
        int BUFFER = 4096; // buffer大小
        String strEntry;

        FileInputStream fis = null;
        ZipInputStream zis = null;

        FileOutputStream fos = null;
        BufferedOutputStream dest = null;

        try {
            fis = new FileInputStream(file);
            zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                Log.d(TAG, "unZip entry = " + entry);
                strEntry = entry.getName();

                if (strEntry.contains("../")) {
                    continue;
                }

                if (entry.isDirectory()) {
                    String entryPath = targetDir + File.separator + strEntry;
                    Log.d(TAG, "unZip entry is folder, path = " + entryPath);
                    File entryFile = new File(entryPath);
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }
                    if (TextUtils.isEmpty(dataDir)) {
                        dataDir = entryFile.getPath();
                    }
                } else {
                    int count;
                    byte data[] = new byte[BUFFER];
                    String targetFileDir = targetDir + File.separator + strEntry;
                    Log.d(TAG, "unZip entry is file, path = " + targetFileDir);
                    File targetFile = new File(targetFileDir);
                    try {
                        fos = new FileOutputStream(targetFile);
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = zis.read(data)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                    } catch (IOException e) {

                        Log.e(TAG, "", e);
                    } finally {
                        try {
                            if (dest != null) {
                                dest.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {

                            Log.e(TAG, "", e);
                        }
                    }
                }
            }
        } catch (IOException e) {

            Log.e(TAG, "", e);
        } finally {
            try {
                if (zis != null) {
                    zis.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {

                Log.e(TAG, "", e);
            }
        }
        return dataDir;
    }

    static class FileCopyTask extends AsyncTask<String, String, Boolean> {

        OnFileCopyListener mListener;
        InputStream mSrcInputStream;
        String mDestPath;

        FileCopyTask(InputStream is, String path) {
            mSrcInputStream = is;
            mDestPath = path;
        }

        public void setOnFileCopyListener(OnFileCopyListener listener) {
            mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null)
                mListener.onCopyStart();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return copyFile(mSrcInputStream, mDestPath);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mListener != null) {
                if (result)
                    mListener.onCopySuccess();
                else
                    mListener.onCopyFailed();
            }
        }
    }

    public interface OnFileCopyListener {
        void onCopyStart();

        void onCopySuccess();

        void onCopyFailed();
    }


    /**
     * 下载文件
     *
     * @param fileUrl
     * @param dstFile
     * @return
     */
    public static boolean downloadFile(@NonNull String fileUrl, @NonNull String dstFile) {
        try {
            URL url = new URL(fileUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
            //int total = conn.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            FileOutputStream fos = new FileOutputStream(dstFile);
            byte buffer[] = new byte[1024];
            //int writen = 0;
            int current;
            while ((current = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, current);
                //writen += current;
                //int progress = (writen * 100) / total;
            }
            fos.flush();
            fos.close();
            bis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 计算文件SHA1值
     *
     * @param file
     * @return
     */
    public static String getSHA1(@NonNull String file) {
        if (new File(file).exists()) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                FileInputStream fis = new FileInputStream(file);
                byte[] bytesBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(bytesBuffer)) != -1) {
                    digest.update(bytesBuffer, 0, bytesRead);
                }
                fis.close();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest.digest()) {
                    int x = b & 0xFF;
                    sb.append(x < 16 ? "0" : "");
                    sb.append(Integer.toHexString(x).toLowerCase());
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 计算文件MD5值
     *
     * @param file
     * @return
     */
    public static String getMD5(@NonNull String file, @NonNull String salt) {
        if (new File(file).exists()) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                FileInputStream fis = new FileInputStream(file);
                byte[] bytesBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(bytesBuffer)) != -1) {
                    digest.update(bytesBuffer, 0, bytesRead);
                }
                digest.update(salt.getBytes());
                fis.close();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest.digest()) {
                    int x = b & 0xFF;
                    sb.append(x < 16 ? "0" : "");
                    sb.append(Integer.toHexString(x).toLowerCase());
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 转为base64
     *
     * @param file
     * @return
     */
    public static String toBase64(@NonNull String file) {
        String result = null;
        try {
            FileInputStream fis = new FileInputStream(new File(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream base64out = new Base64OutputStream(baos, Base64.NO_WRAP);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) >= 0) {
                base64out.write(buffer, 0, len);
            }
            base64out.flush();
            base64out.close();
            /*
             * Why should we close Base64OutputStream before processing the data:
             * http://stackoverflow.com/questions/24798745/android-file-to-base64-using-streaming-sometimes-missed-2-bytes
             */
            result = new String(baos.toByteArray(), "UTF-8");
            baos.close();
            fis.close();
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                           int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    //扫描新加的文件
    public static void scanNewFile(Context context, String path) {
        /*MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
            }
        });*/
        /**
         * 为了防止内存泄漏，需要自己创建 MediaScannerConnectionClient，手动 connect/disconnect，并且传递 ApplicationContext;
         * 参考：https://github.com/square/leakcanary/blob/master/leakcanary-android/src/main/java/com/squareup/leakcanary/AndroidExcludedRefs.java
         */
        new SingleMediaFileScanner(context.getApplicationContext(), new File(path)).start();
    }

    private static class SingleMediaFileScanner implements MediaScannerConnection.MediaScannerConnectionClient {
        private MediaScannerConnection msConn;
        private File mFile;

        public SingleMediaFileScanner(Context context, File file) {
            if (file != null && file.exists() && !file.isDirectory()) {
                mFile = file;
                msConn = new MediaScannerConnection(context, this);
            }
        }

        public void start() {
            if (msConn != null && !msConn.isConnected()) {
                msConn.connect();
            }
        }

        @Override
        public void onMediaScannerConnected() {
            msConn.scanFile(mFile.getAbsolutePath(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (msConn.isConnected()) {
                msConn.disconnect();
            }
            msConn = null;
        }
    }

    public static Uri getUriFromPath(String path) {
        return Uri.fromFile(new File(path));
    }

    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
        if (ctx == null || contentUri == null) {
            return null;
        }
        String path = null;
        Cursor cursor = null;
        try {
            do {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
                if (cursor == null) {
                    break;
                }
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    path = cursor.getString(column_index);
                }
            } while (false);
            return path;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static void copyAssets(Context context, String pAssetFilePath, String pDestFilePath) {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(pAssetFilePath);
            File outFile = new File(pDestFilePath);
            File parent = outFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
