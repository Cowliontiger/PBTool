package com.test.protobuf.util;

import java.io.*;

/**
 * 自定义类加载器
 */
public class MyClassLoader extends ClassLoader {

    private byte[] results;

    public MyClassLoader(String pathName) {
        // 拿到class转成的字节码文件
        results = loadClassFile(pathName);
    }

    /**
     * 把我们的class文件转成字节码，用于classloader动态加载
     * @param classPath
     * @return
     */
    private byte[] loadClassFile(String classPath) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            FileInputStream fi = new FileInputStream(classPath);
            BufferedInputStream bis = new BufferedInputStream(fi);
            byte[] data = new byte[1024 * 256];
            int ch = 0;
            while ((ch = bis.read(data, 0, data.length)) != -1) {
                bos.write(data, 0, ch);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Class<?> loadClass(String arg0, boolean arg1) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(arg0);
        if (clazz == null) {
            if (getParent() != null) {
                try {
                    // 这里我们要用父加载器加载如果加载不成功会抛异常
                    clazz = getParent().loadClass(arg0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (clazz == null) {
                clazz = defineClass(arg0, results, 0, results.length);
            }
        }
        return clazz;
    }
}
