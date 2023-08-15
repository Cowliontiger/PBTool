package com.test.protobuf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * 动态编译
 * 动态加载Class
 */
@Component
public class ClassUtil {

	private final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

	/**
	 * 动态编译
	 */
	private int dynamicCompiler(String filePath) {
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		int flag = javac.run(null, null, null,filePath);
		logger.info(flag == 0 ? "编译成功" : "编译失败");
		return flag;
	}

	/**
	 * 动态加载类
	 * @param fileName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class dynamicLoad(String fileName) throws ClassNotFoundException {
		// 编译文件所在路径
		//String filePath = ConstantUtil.PROTOBUF_JAVA_PATH + fileName;
        String filePath = ConstantUtil.PROTOBUF_FILE_NAME_POSITION + fileName;
        logger.info("filePath:"+filePath);
		if(fileName.equals(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX)){


		    //filePath = ClassLoader.getSystemResource("") +fileName;


		    //filePath = "F:/protobuf-tool/target/classes/AllInOneProtobufProtocol";
            filePath = filePath.replace("file:/","");
        }
		int compilerResult = this.dynamicCompiler(filePath + ".java");
		if (compilerResult == 0) {
			MyClassLoader classLoader = new MyClassLoader(filePath + ".class");

			// 这里要把包路径传入进去

            String path2 = "";
            if(!fileName.equals(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX)){
                path2 = ConstantUtil.PROTOBUF_PACKAGE_NAME+ ".";
            }
            logger.info("path2:"+path2);
            path2= "com.test.protobuf.";
			Class<?> clazz = classLoader.loadClass(path2 + fileName);
			return clazz;
		}
		return null;
	}
	
    public static String getClassNameByProtoId(int protoId){
	    String className = "";
    File fileDir = new File(ConstantUtil.PROTOBUF_FILE_PATH);
    File[] files = fileDir.listFiles();
        for (File file : files) {
            if (file.getName().equals(ConstantUtil.PROTOBUF_FILE_NAME_FROM_GIT)) {
                BufferedReader reader = null;
                //StringBuffer sf = new StringBuffer();
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String tempStr;
                    String _str = "ProtoRspId_";
                    while ((tempStr = reader.readLine()) != null) {
                        if (tempStr.startsWith(_str)) {
                            if(tempStr.indexOf("="+String.valueOf(protoId)+";") > -1){
                                className = tempStr.replace(_str,"").split("=")[0];
                            }
                            //protocolId.add(tempStr.substring(_str.length(), tempStr.length()));
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        return className;
    }


}
