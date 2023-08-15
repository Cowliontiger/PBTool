package com.test.protobuf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 根据.proto文件生成java文件
 */
@Component
public class ProtoUtil {

    private final Logger logger = LoggerFactory.getLogger(ProtoUtil.class);

    /**
     * 根据.proto 文件生成java类
     * @param protoFileName
     * @return className 生成的java文件类名
     */
    public String generationJavaFile(String protoFileName) {
        // .proto 生成的java文件对应的类名
        String className = protoFileName.split("\\.")[0];

//        // 需要执行的命令
//        String cmd = ConstantUtil.PROTOC_EXE_PATH // protoc.exe 文件目录
//           //     + " -I=" + ConstantUtil.PROTOBUF_FILE_PATH // .proto 文件目录
//            + " -I=" + ConstantUtil.PROTOBUF_FILE_NAME_POSITION // .proto 文件目录
//            + " --java_out=" + ConstantUtil.baseClassPath() +"com/test/protobuf" // 生成java文件所在目录
//            //    + " " + ConstantUtil.PROTOBUF_FILE_PATH + File.separator + protoFileName; // .proto 文件
//            + " " + ConstantUtil.PROTOBUF_FILE_NAME_POSITION + protoFileName; // .proto 文件

        // 需要执行的命令
        String cmd = ConstantUtil.PROTOC_EXE_PATH // protoc.exe 文件目录
            + " -I=" + ConstantUtil.PROTOBUF_FILE_PATH // .proto 文件目录
            //+ " --java_out=" + ConstantUtil.baseClassPath() +"com/clg/protobuf/dto"// 生成java文件所在目录
            + " --java_out=" + ConstantUtil.baseClassPath() // 生成java文件所在目录
            + " " + ConstantUtil.PROTOBUF_FILE_PATH + File.separator + protoFileName; // .proto 文件

        logger.info("cmd:"+cmd);
        try {
            // 通过执行cmd命令调用protoc.exe程序生成JAVA文件
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return className;
    }

    /**
     * 测试方法
     * @param args
     */
    public static void main(String[] args) {
        ProtoUtil util = new ProtoUtil();
        String className = util.generationJavaFile("UserRequestDTO.proto");
        String dtoName = className.replace("Request", "");
        try {
            ClassUtil classUtil = new ClassUtil();
            Class clazz = classUtil.dynamicLoad(className);
            String requestClassName = clazz.getName();
            System.out.println("requestClassName：" + requestClassName);
            // 加载内部类
            Class dtoClass = Class.forName(requestClassName + "$" + dtoName);
            System.out.println("dtoClass: " + dtoClass.getName());

            Method method = dtoClass.getMethod("newBuilder", null);
            Object dtoInstance = method.invoke(null);
            System.out.println("dtoInstance.toString():" + dtoInstance.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
