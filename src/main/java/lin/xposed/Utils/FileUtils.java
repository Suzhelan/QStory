package lin.xposed.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static void WriteToFile(String File, String FileContent) {
        try {
            File parent = new File(File).getParentFile();
            if (!parent.exists()) parent.mkdirs();
            BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(File));
            fOut.write(FileContent.getBytes(StandardCharsets.UTF_8));
            fOut.close();
        } catch (Exception e) {
        }
    }

    public static long getDirSize(File file) {
        //判断文件是否存在
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                if (children == null) return 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“兆”为单位
                long size = file.length();
                return size;
            }
        } else {
            return 0;
        }
    }

    public static void WriteToFile(String File, byte[] FileContent) {
        try {
            File parent = new File(File).getParentFile();
            if (!parent.exists()) parent.mkdirs();
            FileOutputStream fOut = new FileOutputStream(File);
            BufferedOutputStream bf = new BufferedOutputStream(fOut);
            bf.write(FileContent);
            bf.close();
            fOut.close();
        } catch (Exception e) {
        }
    }

    public static String ReadFileString(File f) {
        try {
            FileInputStream fInp = new FileInputStream(f);
            String Content = new String(DataUtils.readAllBytes(fInp), StandardCharsets.UTF_8);
            fInp.close();
            return Content;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] ReadFile(File f) {
        try {
            FileInputStream fInp = new FileInputStream(f);
            byte[] Content = DataUtils.readAllBytes(fInp);
            fInp.close();
            return Content;
        } catch (Exception e) {
            return null;
        }
    }

    public static String ReadFileString(String f) {
        return ReadFileString(new File(f));
    }

    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) file.delete();
        File[] files = file.listFiles();
        if (files == null) return;
        //遍历该目录下的文件对象
        for (File f : files) {
            if (f.isDirectory()) {
                deleteFile(f);//目录下有文件夹调用本方法删除
            } else {
                f.delete();
            }
        }
        file.delete();
    }


    public static void copy(String source, String dest) {

        try {

            File f = new File(dest);
            f = f.getParentFile();
            if (!f.exists()) f.mkdirs();

            File aaa = new File(dest);
            if (aaa.exists()) aaa.delete();

            InputStream in = new FileInputStream(new File(source));
            OutputStream out = new FileOutputStream(new File(dest));
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
        }
    }
}
