package com.test.protobuf.dto;

import com.google.protobuf.Message;

import java.io.Serializable;


/**
 * 请求参数
 */
public class RequestDTO implements Serializable {

	private static final long serialVersionUID = -8781054994456196457L;

	/**
	 * 接口地址
	 */
	private String interfaceUrl;

	/**
	 * 请求类型
	 */
	private String methodType;

	/**
	 * 协议
	 */
	private String protocolType;

	/**
	 * .proto 文件名
	 */
	private String protoFileName;


    /**
     * protocolId
     */
    private String protocolIdName;

	/**
	 * 请求字符串参数
	 */
	private String paramData;

    /**
     * 请求protobuf对象参数
     */
    private Message.Builder paramObjectData;

	public String getInterfaceUrl() {
		return interfaceUrl;
	}

	public void setInterfaceUrl(String interfaceUrl) {
		this.interfaceUrl = interfaceUrl;
	}

	public String getMethodType() {
		return methodType;
	}

	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	public String getProtoFileName() {
		return protoFileName;
	}

	public void setProtoFileName(String protoFileName) {
		this.protoFileName = protoFileName;
	}

	public String getParamData() {
		return paramData;
	}

	public void setParamData(String paramData) {
		this.paramData = paramData;
	}

    public Message.Builder getParamObjectData() {
        return paramObjectData;
    }

    public void setParamObjectData(Message.Builder paramObjectData) {
        this.paramObjectData = paramObjectData;
    }

    public String getProtocolIdName() { return protocolIdName;}

    public void setProtocolIdName(String protocolIdName) { this.protocolIdName = protocolIdName; }


	@Override
	public String toString() {
		return "RequestDTO{" +
				"interfaceUrl='" + interfaceUrl + '\'' +
				", methodType='" + methodType + '\'' +
				", protocolType='" + protocolType + '\'' +
				", protoFileName='" + protoFileName + '\'' +
				", paramData='" + paramData + '\'' +
                ", protocolIdName='" + protocolIdName + '\'' +
				'}';
	}
}
