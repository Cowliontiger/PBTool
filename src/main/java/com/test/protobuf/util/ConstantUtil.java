package com.test.protobuf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class ConstantUtil {

	/**
	 * 请求数据类型
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * 接受数据类型
	 */
	public static final String ACCEPT = "Accept";

	/**
	 * 请求类型：POST
	 */
	public static final String REQUEST_METHOD_TYPE_POST = "POST";

	/**
	 * 请求类型：GET
	 */
	public static final String REQUEST_METHOD_TYPE_GET = "GET";

    /**
     * 请求类型：SOCKET
     */
    public static final String REQUEST_METHOD_TYPE_SOCKET = "SOCKET";

	/**
	 * 数据传输类型：json 格式
	 */
	public static final String MEDIA_TYPE_JSON = "application/json";

	/**
	 * 数据传输类型：protobuf 格式
	 */
	public static final String MEDIA_TYPE_PROTOCOL_BUFFER = "application/x-protobuf";

	/**
	 * 数据传输类型：json 格式
	 */
	public static final String MEDIA_TYPE_JSON_URF_8 = "application/json;charset=utf-8";

	/**
	 * 数据传输类型：protobuf 格式
	 */
	public static final String MEDIA_TYPE_PROTOCOL_BUFFER_URF_8 = "application/x-protobuf;charset=utf-8";

	/**
	 * class文件磁盘根目录
	 * @return
	 */
	public static String baseClassPath() {
		String baseClassPath = ConstantUtil.class.getResource("/").toString();
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") != -1) {
			if (baseClassPath.startsWith("file:/")) {
				baseClassPath = baseClassPath.substring(6);
			}
		}
		return baseClassPath;
	}

	/**
	 * 项目根目录
	 */
    public static final String PROJECT_ROOT = System.getProperty("user.dir")+"/";

	/**
	 * 根据.proto 文件生成java对象包目录
	 */
    public static final String PROTOBUF_JAVA_PATH = baseClassPath() + "com/test/protobuf/dto/";

	/**
	 * 根据.proto 文件生成java对象包名
	 */
	public static final String PROTOBUF_PACKAGE_NAME = "com.test.protobuf.dto";

    /**
     * java对象包名
     */
    public static final String JAVA_PACKAGE_NAME = "com.test.protobuf";

    /**
     * 点号
     */
    public static final String Dot_STRING = ".";

	/**
	 * .proto 文件所在目录
	 */
	public static final String PROTOBUF_FILE_PATH = PROJECT_ROOT + "proto";

	/**
	 * 编译proto文件
	 */
	public static final String PROTOC_EXE_PATH = PROJECT_ROOT + "protoc/bin/protoc.exe";

    /**
     * 从github下载proto文件路径
     */
    public static final String PROTOBUF_FILE_PATH_FROM_GIT = "http://raw//master//proto//";

    /**
     * 从github下载proto文件名称
     */
    public static final String PROTOBUF_FILE_NAME_FROM_GIT = "All.proto";

    /**
     * proto文件名称,不含后缀
     */
    public static final String PROTOBUF_FILE_NAME_WITHOUT_SUFFIX = "All";

    /**
     * proto文件位置
     */
    public static final String PROTOBUF_FILE_NAME_POSITION = "F:\\ProtoBufTool\\target\\classes\\com\\test\\protobuf\\";
    /**
     * protocolId protocolId数据
     */
    public static List<String> protocolId = new ArrayList<String>();

    /**
     * 获取所有的protocolId数据
     */
    static {
        File fileDir = new File(PROTOBUF_FILE_NAME_POSITION);
        File[] files = fileDir.listFiles();
        for (File file : files) {
            if(file.getName().equals(PROTOBUF_FILE_NAME_FROM_GIT)){
                BufferedReader reader = null;
                //StringBuffer sf = new StringBuffer();
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String tempStr;
                    String _strReq = "ProtoReqId_";
                    String _strRes = "ProtoRspId_";
                    while ((tempStr = reader.readLine()) != null) {
                        if(tempStr.startsWith(_strReq) || tempStr.startsWith(_strRes)) {
                            protocolId.add(tempStr.substring(_strReq.length(),tempStr.length()));
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
    }

	/**
	 * protoFile 文件
	 */
	public static List<String> protoFile = new ArrayList<String>();


	/**
	 * 获取所有的.proto文件名
	 */
	static {
		File fileDir = new File(PROTOBUF_FILE_PATH);
		File[] files = fileDir.listFiles();
		if (files.length > 0) {
			for (File file : files) {
				String fileName = file.getName();
				if (fileName.endsWith(".proto")) {
				    if(!fileName.startsWith("UserRequestDTO")) {
                        protoFile.add(fileName);
                    }
				}
			}
		}
	}

	public static void main(String[] args) {
		System.out.println(baseClassPath());
	}
}
