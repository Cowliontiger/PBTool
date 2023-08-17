package com.test.protobuf.util;

import com.test.protobuf.dto.RequestDTO;
import com.test.protobuf.dto.ResponseDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.*;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


/**
 * http请求发送工具类
 * 发送 SOCK 、GET 和 POST 请求
 * 支持 JSON 和 Protocol Buffer 数据格式
 */




@Component
public class HttpClientUtil {

	Socket s=null;
	boolean isClient=false;
	String responseBody="";

	private final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	private final String log_prefix = "==================== HttpClientUtil execute ";

    public ResponseDTO executeHttpRequest(RequestDTO requestDTO, Class clazz,String protocolId)
			//throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        throws Exception {
		//logger.info(log_prefix + " executeHttpRequest start =>");
		long startTime = System.currentTimeMillis();
		ResponseDTO responseDTO;
		// GET请求
		if (ConstantUtil.REQUEST_METHOD_TYPE_GET.equalsIgnoreCase(requestDTO.getMethodType())) {
			//logger.info(log_prefix + " Request method is GET");
			responseDTO = this.get(requestDTO, clazz);
		} else {
			//logger.info(log_prefix + " Request method is POST");
			responseDTO = this.post(requestDTO, clazz,protocolId);
		}
		long endTime = System.currentTimeMillis();
		//logger.info(log_prefix + " getProtobuf end => 共执行时间：" + (endTime - startTime) + " ms");
		return responseDTO;
	}

	/**
	 * 发送GET请求
	 * @param requestDTO
	 * @param clazz
	 * @return
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public ResponseDTO get(RequestDTO requestDTO, Class<?> clazz)
			throws Exception {
		ResponseDTO responseDTO = new ResponseDTO();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		// 设置请求超市时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
				.setSocketTimeout(5000).build();
		// 接口地址
		String interfaceUrl = requestDTO.getInterfaceUrl();
		// 构建请求参数
		String requestParam = this.getRequestParam(requestDTO.getParamData());
		if (!StringUtils.isEmpty(requestParam)) {
			if (interfaceUrl.indexOf("?") == -1) {
				interfaceUrl += "?" + requestParam;
			} else {
				interfaceUrl += "&" + requestParam;
			}
		}
		HttpGet httpGet = new HttpGet(interfaceUrl);
		httpGet.setConfig(requestConfig);
		// 请求数据类型为：application/x-protobuf
		if (ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER.equalsIgnoreCase(requestDTO.getProtocolType())) {
			httpGet.addHeader(ConstantUtil.CONTENT_TYPE, ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER_URF_8);
			httpGet.addHeader(ConstantUtil.ACCEPT, ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER_URF_8);
		} else {
			// 请求数据类型为：application/json
			httpGet.addHeader(ConstantUtil.CONTENT_TYPE, ConstantUtil.MEDIA_TYPE_JSON_URF_8);
			httpGet.addHeader(ConstantUtil.ACCEPT, ConstantUtil.MEDIA_TYPE_JSON_URF_8);
		}
		// 获取请求头信息
		responseDTO.setRequestHeader(this.requestHeaderGet(httpGet));
		HttpResponse httpResponse = httpClient.execute(httpGet);
		// 获取响应头信息
		String responseHeader = this.responseHeader(httpResponse);
		responseDTO.setResponseHeader(responseHeader);
		// 解析响应数据
		String responseBody = this.getResponseData(httpResponse, clazz);
		responseDTO.setResponseBody(responseBody);
		return responseDTO;
	}

	/**
	 * 发送POST请求
	 * @param requestDTO
	 * @param clazz
	 * @return
	 */
	public ResponseDTO post(RequestDTO requestDTO, Class<?> clazz,String protocolId)
			//throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        throws Exception {
        String responseBody = "";
		ResponseDTO responseDTO = new ResponseDTO();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		// 设置请求超时时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(60000).setConnectionRequestTimeout(10000)
				.setSocketTimeout(60000).build();
		HttpPost httpPost = new HttpPost(requestDTO.getInterfaceUrl());
		httpPost.setConfig(requestConfig);
		StringBuffer requestBody = new StringBuffer();
		// 请求数据类型为：application/x-protobuf
		if (ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER.equalsIgnoreCase(requestDTO.getProtocolType())) {
			httpPost.addHeader(ConstantUtil.CONTENT_TYPE, ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER_URF_8);
			httpPost.addHeader(ConstantUtil.ACCEPT, ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER_URF_8);
			byte[] bytes = new byte[1000];
            if(clazz != null) {
                if(requestDTO.getParamData() != null) {
                    bytes = this.jsonToProtobufByteArray(requestDTO.getParamData(), clazz);
                } else if(requestDTO.getParamObjectData() != null){
                    bytes = requestDTO.getParamObjectData().build().toByteArray();
                }
                requestBody.append("Request Param Protocol Buffer Data: ");
                for (byte b : bytes) {
                    requestBody.append(b);
                }
                requestBody.append("<br /><br />");
                requestBody.append("Request Param  JSON Data : ").append(requestDTO.getParamData()).append("<br />");
                httpPost.setEntity(new ByteArrayEntity(bytes));
            }
		} else {
			// 请求数据类型为：application/json
			httpPost.addHeader(ConstantUtil.CONTENT_TYPE, ConstantUtil.MEDIA_TYPE_JSON_URF_8);
			httpPost.addHeader(ConstantUtil.ACCEPT, ConstantUtil.MEDIA_TYPE_JSON_URF_8);
			httpPost.setEntity(new ByteArrayEntity(requestDTO.getParamData().getBytes()));
			requestBody.append("Request Param  JSON Data : ").append(requestDTO.getParamData()).append("<br />");
		}

        if(ConstantUtil.REQUEST_METHOD_TYPE_SOCKET.equalsIgnoreCase(requestDTO.getMethodType())){
            //JsonParser parser = new JsonParser();
            //JsonElement element = parser.parse(requestDTO.getParamData());
            //JsonObject root = element.getAsJsonObject();
            //int protocolId = root.get("protocolId").getAsInt();
            logger.info("protocolId:"+protocolId);
            if(requestDTO.getParamData() != null) {
                responseBody = this.send(requestDTO.getParamData(),
                    clazz, Integer.valueOf(protocolId),
                    httpPost.getURI().getHost(),
                    Integer.valueOf(httpPost.getURI().getPort()));
            } else if (requestDTO.getParamObjectData() != null){
                responseBody = this.send(requestDTO.getParamObjectData(),
                    clazz, Integer.valueOf(protocolId),
                    httpPost.getURI().getHost(),
                    Integer.valueOf(httpPost.getURI().getPort()));
            }
        }



        if(ConstantUtil.REQUEST_METHOD_TYPE_POST.equalsIgnoreCase(requestDTO.getMethodType())
            ||ConstantUtil.REQUEST_METHOD_TYPE_GET.equalsIgnoreCase(requestDTO.getMethodType()) ){

            // 获取请求头信息
            responseDTO.setRequestHeader(this.requestHeaderPost(httpPost));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            // 设置请求参数信息
            responseDTO.setRequestBody(requestBody.toString());
            // 获取响应头信息
            String responseHeader = this.responseHeader(httpResponse);
            responseDTO.setResponseHeader(responseHeader);
            // 解析响应数据
            responseBody = this.getResponseData(httpResponse, clazz);

        }

		responseDTO.setResponseBody(responseBody);
		return responseDTO;
	}

	/**
	 * 获取GET请求头信息
	 * @param httpGet
	 * @return
	 */
	private String requestHeaderGet(HttpGet httpGet) {
		Header[] requestHeaders = httpGet.getAllHeaders();
		return this.getHeaderInfo(requestHeaders);
	}

	/**
	 * 获取POST请求头信息
	 * @param httpPost
	 * @return
	 */
	private String requestHeaderPost(HttpPost httpPost) {
		Header[] responseHeaders = httpPost.getAllHeaders();
		return this.getHeaderInfo(responseHeaders);
	}

	/**
	 * 获取响应头信息
	 * @param httpResponse
	 * @return
	 */
	private String responseHeader(HttpResponse httpResponse) {
		Header[] responseHeader = httpResponse.getAllHeaders();
		return this.getHeaderInfo(responseHeader);
	}

	/**
	 * 获取请求头信息
	 * @param headers
	 * @return
	 */
	private String getHeaderInfo(Header[] headers) {
		StringBuffer headerInfo = new StringBuffer();
		for (Header header : headers) {
			headerInfo.append(header.getName()).append(":").append(header.getValue()).append("<br />");
		}
		return headerInfo.toString();
	}

	/**
	 * 将IO流数据转换成ProtocolBuffer的java对象
	 * @param protocolBufferStream  protocolBuffer流数据
	 * @param tClass 转换目标类型
	 * @return ProtocolBuffer的java对象
	 * @throws Exception
	 */
	public Object convertIOStreamToProtoBufObjcet(InputStream protocolBufferStream, Class<?> tClass)
			throws Exception {
		Parser<Message> parser;
		Method method = tClass.getDeclaredMethod("parser");
		parser = (Parser<Message>) method.invoke(null);
		Object object = parser.parseFrom(protocolBufferStream);
		return object;
	}

    /**
     * ProtocolBuffer的json模板
     * @param tClass 转换目标类型
     * @return ProtocolBuffer的json模板
     * @throws Exception
     */
    public String genJsonTemplate(Class<?> tClass)
        throws Exception {

        Method buildM = tClass.getDeclaredMethod("newBuilder");
        AbstractMessage.Builder<?> builder = (AbstractMessage.Builder<?>) buildM.invoke(null);
        String templateJson = null;
        templateJson  =  com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(builder.build());

        StringBuilder sb = new StringBuilder();
        Message.Builder msgBuilder = (Message.Builder)builder;
        //Descriptors.Descriptor descriptor = msgBuilder.getDescriptorForType();

        Descriptors.Descriptor requestDesc = msgBuilder.getDescriptorForType();
        List<Descriptors.FieldDescriptor> requestFields = requestDesc.getFields();
        Iterator<Descriptors.FieldDescriptor> iter = requestFields.iterator();
        int descriptorsNum = requestFields.size();
        int flag = 0;
        while (iter.hasNext()) {
            flag++;
            Descriptors.FieldDescriptor fd = iter.next();
            //TODO: deal with repeated fields
            //sb.append("{\"name\":\"");
            //sb.append(fd.getName());
            //sb.append("\",\"type\":\"");
            if (fd.getType().toString().equalsIgnoreCase("message")) {
                if(templateJson.indexOf(",") == -1){
                    sb.append(",");
                }
                if(descriptorsNum == flag )
                    sb.append(",\"");
                else
                    sb.append("\"");


                //如果值是AvatarInfo的话就打印，如果是xxx.xxx的话就不打印
                if(fd.getMessageType().getFullName().indexOf(".") == -1) {
                    sb.append(fd.getMessageType().getName());
                    sb.append("\":");
                }
                Descriptors.FieldDescriptor childDescriptor = requestDesc.findFieldByName(fd.getName());
                Message.Builder subMessageBuilder = builder.newBuilderForField(childDescriptor);
                Message subMessage = subMessageBuilder.build();

                Descriptors.Descriptor requestDesc2 = subMessage.getDescriptorForType();
                List<Descriptors.FieldDescriptor> requestFields2 = requestDesc2.getFields();
                Iterator<Descriptors.FieldDescriptor> iter2 = requestFields2.iterator();
                int descriptorsNum2 = requestFields.size();
                int isAllNotMessageType = 0;
                while (iter2.hasNext()) {
                    Descriptors.FieldDescriptor fd2 = iter2.next();

                    if (fd2.getType().toString().equalsIgnoreCase("message")) {
                        String subClassName = requestDesc2.getFields().get(1).getMessageType().getFullName();
                        logger.info(requestDesc2.getFields().get(1).getMessageType().getFullName());
                        //logger.info(getObjectByObjectFromProto("AllInOneProtobufProtocol$HomeChangeItem").toString());
                        //sb.append(getObjectByObjectFromProto(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX+"$"+subClassName).toString());
                        isAllNotMessageType++;
                        sb.append(genJsonTemplate2(Class.forName(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX+"$"+subClassName)));

                    } else {

                    }
                }
                if(isAllNotMessageType == 0){
                    sb.append(com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(subMessage));
                }

            }

        }
        templateJson = templateJson.replaceFirst("}",sb.toString()+"}");
        //logger.info("templateJson:" + templateJson);



        return templateJson;
    }


    /**
     * ProtocolBuffer的json模板
     * @param tClass 转换目标类型
     * @return ProtocolBuffer的json模板
     * @throws Exception
     */
    public String genJsonTemplate2(Class<?> tClass)
        throws Exception {

        Method buildM = tClass.getDeclaredMethod("newBuilder");
        AbstractMessage.Builder<?> builder = (AbstractMessage.Builder<?>) buildM.invoke(null);
        String templateJson = null;
        templateJson  =  com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(builder.build());
        StringBuilder sb = new StringBuilder();
        Message.Builder msgBuilder = (Message.Builder)builder;
        //Descriptors.Descriptor descriptor = msgBuilder.getDescriptorForType();

        Descriptors.Descriptor requestDesc = msgBuilder.getDescriptorForType();
        List<Descriptors.FieldDescriptor> requestFields = requestDesc.getFields();
        Iterator<Descriptors.FieldDescriptor> iter = requestFields.iterator();
        //int descriptorsNum = requestFields.size();
        //int flag = 0;
        while (iter.hasNext()) {
            //flag++;
            Descriptors.FieldDescriptor fd = iter.next();
            //TODO: deal with repeated fields
            //sb.append("{\"name\":\"");
            //sb.append(fd.getName());
            //sb.append("\",\"type\":\"");
            if (fd.getType().toString().equalsIgnoreCase("message")) {
                //if(descriptorsNum == flag )
                //    sb.append(",\"");
                //else
                //    sb.append("\"");
                sb.append(fd.getMessageType().getFullName());
                //sb.append("\":{\"");
                sb.append("\":");
                Descriptors.FieldDescriptor childDescriptor = requestDesc.findFieldByName(fd.getName());
                Message.Builder subMessageBuilder = builder.newBuilderForField(childDescriptor);
                Message subMessage = subMessageBuilder.build();

                Descriptors.Descriptor requestDesc2 = subMessage.getDescriptorForType();
                List<Descriptors.FieldDescriptor> requestFields2 = requestDesc2.getFields();
                Iterator<Descriptors.FieldDescriptor> iter2 = requestFields2.iterator();
                int descriptorsNum2 = requestFields.size();
                int isAllNotMessageType = 0;
                while (iter2.hasNext()) {
                    Descriptors.FieldDescriptor fd2 = iter2.next();

                    if (fd2.getType().toString().equalsIgnoreCase("message")) {
                        String subClassName = requestDesc2.getFields().get(1).getMessageType().getFullName();
                        //logger.info(requestDesc2.getFields().get(1).getMessageType().getFullName());
                        //logger.info(getObjectByObjectFromProto("AllInOneProtobufProtocol$HomeChangeItem").toString());
                        //sb.append(getObjectByObjectFromProto(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX+"$"+subClassName).toString());
                        isAllNotMessageType++;
                    } else {

                    }
                }
                if(isAllNotMessageType == 0){
                    sb.append(com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(subMessage));
                }

            }

        }
        //templateJson = templateJson.replaceFirst("}",sb.toString()+"}");
        templateJson = templateJson.replaceFirst("\\{","{"+sb.toString()+",");
        logger.info("templateJson:" + templateJson);

        templateJson = templateJson.replaceFirst("\\{","");
        templateJson = templateJson.substring(0,templateJson.length() - 1);

        return templateJson;
    }




    /**
     * 将IO流数据转换成ProtocolBuffer的java对象
     * @param tClass 转换目标类型
     * @return ProtocolBuffer的java对象
     * @throws Exception
     */
    public Object convertBytesToProtoBufObjcet(byte[] bytes, Class<?> tClass)
        throws Exception {
        int BodyLen = 0;
        int ProtoId = 0;
        byte[] _receiveBodyLen,_receiveProtoId,_receiveBodyBytes = null;
        //int flag = 0;
        int newStartPosition = 0;
        String jsonStr = "";
        String className = "";
        StringBuffer sbf = new StringBuffer();
        while(true){
            //flag++;
            _receiveBodyLen = reverseByteArray(Arrays.copyOfRange(bytes,newStartPosition,newStartPosition+4));
            _receiveProtoId = reverseByteArray(Arrays.copyOfRange(bytes,newStartPosition+4,newStartPosition+8));
            BodyLen = BitConverter.ToInt32(_receiveBodyLen,0);
            ProtoId = BitConverter.ToInt32(_receiveProtoId,0);
            if(BodyLen < 0)
                break;
            _receiveBodyBytes = Arrays.copyOfRange(bytes,newStartPosition+8,newStartPosition+8+BodyLen);

            //logger.info("Response data:"+flag);
            logger.info("BodyLen:"+BodyLen);
            logger.info("ProtoId:"+ProtoId);
            logger.info("ClassName:"+ClassUtil.getClassNameByProtoId(ProtoId));
            newStartPosition += 8 + BodyLen;
            if(BodyLen > 10000 || BodyLen < 0 || ProtoId == 0  || String.valueOf(ProtoId).length() > 8) {
                break;
            }else{
				className = ClassUtil.getClassNameByProtoId(ProtoId);
				sbf.append("接口名称:"+className+"\n");


                jsonStr = genJsonByProtoId(className,_receiveBodyBytes);
                //打印全部push广播日志
                //logger.info("ProtoBuf json:"+jsonStr);
                //sbf.append(flag+":<br>");
                sbf.append(jsonStr+"<br>");
                //StatusTipsRsp||ServerExceptionResponse||ServerCloseRsp
                if (ProtoId == 4 || ProtoId == 3 || ProtoId == 500) {
                    //sbf.append("{\"ProtoId\":"+ProtoId+"}<br>");
                    sbf.append(sbf.toString().replace("}","\"ProtoId\":"+ProtoId+" }"));
                    break;
                }
            }
        }


        //Parser<Message> parser2 = (Parser<Message>) Class.forName(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX+"$PlayerDataPush").getDeclaredMethod("parser").invoke(null);
        //Message.Builder ms = parser2.parseFrom(bytes,13,54).toBuilder();

        //Object object2 = com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(ms);


        return sbf;
        //return convertProtobufToJson((Message) object);
    }

    public String genJsonByProtoId(String className,byte[] bytes) throws Exception{
        String jsonStr = "";
        //Parser<Message> parser = (Parser<Message>) Class.forName(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX+"$"+className).getDeclaredMethod("parser").invoke(null);
        //Message.Builder ms = parser.parseFrom(bytes).toBuilder();
        //jsonStr = com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(ms);
        Parser<Message> parser;
        try {
        //todo
        //if(!className.equals("JoinRoomRsp")) {
            Method method = Class.forName(ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX + "$" + className).getDeclaredMethod("parser");
            parser = (Parser<Message>) method.invoke(null);
            jsonStr += com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(
                parser.parseFrom(bytes, 0, bytes.length).toBuilder());
        //}
        } catch (ClassNotFoundException e){
            logger.info("The *Push or *Class" + className +" is not exist!");
        }
        return jsonStr;
    }

	/**
	 * 将 Protocol Buffer 转换成 JSON
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public String convertProtobufToJson(Message message) throws IOException {
		JsonFormat jsonFormat = new JsonFormat();
		return jsonFormat.printToString(message);
	}

    /**
     * 倒置字节数组
     *
     * @param arr
     *            byte数组
     * @return byte数组
     */
    public static byte[] reverseByteArray(byte[] arr) {
        for(int start=0,end=arr.length-1;start<end;start++,end--) {
            byte temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
        }
        return arr;
    }


    public static int toInt32_2(byte[] bytes, int index)
    {
        int a = (int)((int)(0xff & bytes[index]) << 32 | (int)(0xff & bytes[index + 1]) << 40 | (int)(0xff & bytes[index + 2]) << 48 | (int)(0xff & bytes[index + 3]) << 56);
        // int a = (int)((int)(0xff & bytes[index]) << 56 | (int)(0xff & bytes[index + 1]) << 48 | (int)(0xff & bytes[index + 2]) << 40 | (int)(0xff & bytes[index + 3]) << 32);
        //Array.Resize;
        return a;
    }

    /**
	 * 将Json 转换为Map<String, String>
	 * @param jsonString
	 * @return
	 */
	private Map<String, String> jsonToMap(String jsonString) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		Type type = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> map = gson.fromJson(jsonString, type);
		return map;
	}

	/**
	 * 构建请求参数
	 * @param requestParam
	 * @return
	 */
	private String getRequestParam(String requestParam) throws IOException {
		if (StringUtils.isEmpty(requestParam)) {
			return null;
		}
		Map<String, String> paramMap = this.jsonToMap(requestParam);
		List<NameValuePair> params = Lists.newArrayList();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			logger.info("====================请求参数：" + key + " 的值为：" + value);
			params.add(new BasicNameValuePair(key, value));
		}
		String paramData = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
		return paramData;
	}

	/**
	 * 将JSON转换为ProtocolBuffer的byte数组
	 * @param jsonString json字符串
	 * @param tClass 转换目标类型
	 * @return
	 */
	private byte[] jsonToProtobufByteArray(String jsonString, Class<?> tClass)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
		Method method = tClass.getDeclaredMethod("newBuilder");
		Message.Builder builder = (Message.Builder) method.invoke(null);
		JsonFormat jsonFormat = new JsonFormat();
		jsonFormat.merge(new ByteArrayInputStream(jsonString.getBytes()), builder);
		return builder.build().toByteArray();
	}

	/**
	 * 获取返回数据
	 * @param httpResponse http 请求响应数据
	 * @param clazz
	 * @return responseData 响应数据
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private String getResponseData(HttpResponse httpResponse, Class<?> clazz)
			throws Exception {
		StringBuffer responseBody = new StringBuffer();
		// 返回数据类型
		String responseContentType = httpResponse.getEntity().getContentType().toString();
		// 返回数据类型为：application/x-protobuf1
		if (responseContentType.indexOf(ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER) != -1) {
			InputStream responseStream = httpResponse.getEntity().getContent();
			// 将 Protocol Buffer 转换成 JSON
			Object protoBufDTO = this.convertIOStreamToProtoBufObjcet(responseStream, clazz);
			Method toByteArrayMethod = protoBufDTO.getClass().getMethod("toByteArray", null);
			byte[] byteData = (byte[]) toByteArrayMethod.invoke(protoBufDTO,null);
			responseBody.append("Response Protocol Buffer Data: ");
			if (byteData.length > 0) {
				for (byte b : byteData) {
					responseBody.append(b);
				}
			}
//			responseBody.append("Class:"+clazz);
			responseBody.append("<br /><br /><br />");
			// JSON 格式数据
			Message message = (Message) protoBufDTO;
			String jsonData = this.convertProtobufToJson(message);
			responseBody.append("Response JSON Data : ").append(jsonData).append("<br/>");
		} else {
			// 返回数据类型为：application/json
			String responseContent = EntityUtils.toString(httpResponse.getEntity());
			responseBody.append("Response JSON Data : ").append(responseContent).append("<br/>");
		}
		return responseBody.toString();
	}


    /**
     * 获取返回数据
     * @param <T> requestBuilder 支持请求对象builder或者String等多种类型对象参数
     * @param clazz
     * @param protocolId 协议ID
     * @param host sockip
     * @param port sockport
     * @return responseData 响应数据
     * @throws Exception
     */

    public <T> String send(T requestBuilder, Class clazz, int protocolId, String host, int port) throws Exception{



        logger.info("requestStr clazz:"+clazz.getName());
        //logger.info("requestStr after:"+"{ "+requestStr.substring(requestStr.split(",")[0].length()+1,requestStr.length()));
        //requestStr = "{ "+requestStr.substring(requestStr.split(",")[0].length()+1,requestStr.length());
        logger.info("requestStr:"+requestBuilder);
        if(s.isClosed())
            s = new Socket(host,port);
        //构建IO
        DataOutputStream output = new DataOutputStream(s.getOutputStream());
        //向服务器端发送一条消息
        //bw.write("测试客户端和服务器通信，服务器接收到消息返回到客户端\n");
        byte[] headBytes = new byte[]{102, 97, 115, 116};
        //byte[] bodyBytes = this.jsonToProtobufByteArray(requestStr, clazz);
        //byte[] bodyBytes = ((Message.Builder)requestBuilder).build().toByteArray();
        byte[] bodyBytes = null;
        //如果是String类型的,就走send String
        if(requestBuilder.getClass().getTypeName().equals("java.lang.String")){
            bodyBytes = this.jsonToProtobufByteArray((String)requestBuilder, clazz);
        } else {
            //类似com.test.protobuf.AllInOneProtobufProtocol$InteractActionReq$Builder的类型，就走Builder类封闭
            bodyBytes = ((Message.Builder)requestBuilder).build().toByteArray();
        }

        ByteBuffer buf = ByteBuffer.allocate(16 + bodyBytes.length);
        buf.put(headBytes);
        buf.putInt(bodyBytes.length);
        buf.putInt(protocolId);
        buf.putInt(0);
        buf.put(bodyBytes);
        output.write(buf.array());
        output.flush();
        //output.close();
        //读取服务器返回的消息
        //start();
        //BufferedReader br = new BufferedReader(ne w InputStreamReader(is));
        DataInputStream input = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        byte[] buffer = new byte[20480];
        StringBuffer sBuf = new StringBuffer();
        //消息长度
        int flag = 0;
        do{
            //logger.info("循环次数："+flag);
            int rlength = input.read(buffer, 0, 20480);
            logger.info("接收的消息长度:" + rlength);
            String bufferStr = new String(buffer);
            sBuf.append(bufferStr);
            String requestClassName = clazz.getName();
            String responseClassName = requestClassName.replace("Req", "Rsp");
            try {
                Class cls = Class.forName(responseClassName);
                Object protoBufDTO = this.convertBytesToProtoBufObjcet(buffer, cls);
                responseBody += protoBufDTO.toString();
            } catch (ClassNotFoundException e){
                logger.info("The " + responseClassName +" is not exist!");
            }
            //Thread.sleep(2000);
            //new Thread(new ProtobufReceive(port, "push")).start();
        } while(input.available() != 0);
        return responseBody;
    }

    public void run(){
        DataInputStream input = null;
        try {
            input = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        }catch(Exception e){
	        e.printStackTrace();
        }
            byte[] buffer = new byte[20480];

            while(true){
            try {
                //消息长度
                int rlength = input.read(buffer, 0, 20480);
                logger.info("接收的消息长度:" + rlength);
                //传输的实际byte[]
                byte[] buffer1 = new byte[rlength];
                for (int i = 10; i < buffer1.length; i++) {
                    buffer1[i] = buffer[i];
                }

                String buffer1Str = new String(buffer1);
                //boolean isGBK = java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(buffer1Str);
                //logger.info("GBK:"+isGBK);
                //logger.info("UTF-8:"+java.nio.charset.Charset.forName("UTF-8").newEncoder().canEncode(buffer1Str));
        /*
        if(!isGBK) {
            String requestClassName = clazz.getName();
            String responseClassName = requestClassName.replace("Req", "Rsp");
            Class cls = Class.forName(responseClassName);
            Object protoBufDTO = this.convertIOStreamToProtoBufObjcet(new BufferedInputStream(s.getInputStream()), cls);
            Method toByteArrayMethod = protoBufDTO.getClass().getMethod("toByteArray", null);
            byte[] byteData = (byte[]) toByteArrayMethod.invoke(protoBufDTO, null);
            String buffer2Str = new String(byteData);
            logger.info("Decoded not gbk String:" + buffer2Str);
            responseBody = buffer2Str;
        } else {
         */

                logger.info("Decoded gbk String:" + buffer1Str);
                responseBody = buffer1Str;
            } catch (Exception e){
                e.printStackTrace();
            }

    }
	}
/**
	 * 连接socket
	 * @throws Exception
	 */
	public boolean initSocket(String host,int port) throws IOException {
		try{
			s= new Socket(host,port);
			//s.setKeepAlive(true);
			logger.info("socket连接成功"+s);
			checkClient();
		}
		catch (IOException e){
			logger.error("socket连接失败"+s);
			logger.error(e.toString());
		}
		finally {
			return  checkSocket();
		}
	}

	/**
	 * 检查心跳
	 * @throws Exception
	 */
	public  boolean checkClient(){
		//System.out.println("checkClient");
		try{
			int flag = 0 ;
			//logger.info(String.valueOf("连接次数："+flag++));
			//s.sendUrgentData(0xFF); // 发送心跳包
			logger.info("socket处于链接状态。");
			isClient=true;
		}
		catch (Exception e) {
			e.printStackTrace();
			isClient=false;
		}
		return  isClient;
	}

	/**
	 * 获取显示广播(push)信息
	 * @throws Exception
	 */
	public  String  getRadio() throws Exception {
		logger.info("getradio");

		StringBuffer sBuf = new StringBuffer();
		DataInputStream input = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		byte[] buffer = new byte[20480];
		String	retSting="";
		if(input.available() != 0) {
			int rlength = input.read(buffer, 0, 20480);
			//logger.info("接收的消息长度:" + rlength);
			String bufferStr = new String(buffer);
			sBuf.append(bufferStr);
			Object protoBufDTO = this.convertBytesToProtoBufObjcet(buffer, null);
			//logger.info("接收的消息内容:" + protoBufDTO);
			retSting=protoBufDTO.toString();
//			System.out.println("retSting"+retSting);
		}else{
            logger.info("无广播内容" );
		}
		return retSting;
	}

	/**
	 * 断开socket
	 * @throws Exception
	 */
	public boolean closeSocket() throws IOException {
		try{
			if (s.isConnected()){
				s.close();
				logger.info("socket关闭"+s);
			}

		}
		catch (IOException e){
			logger.error("socket关闭失败，"+s);
			logger.error(e.toString());
		}
		finally {
			return  checkSocket();

		}

	}
	/**
	 * 检查socket状态
	 * @throws Exception
	 */
	public boolean checkSocket()  {
        isClient = s.isConnected();
		return  isClient;

	}
	public static class TCPClient implements Runnable{
		public static void main(String[] args) throws IOException {
			new TCPClient().init();
		}
		private void init() throws IOException{
			@SuppressWarnings("resource")
			final Socket client = new Socket("192.168.20.213",8880);
			System.out.println("初始化服务器"+client);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String send = "{ \"protocolId\":1007,\"gender\":2}";
			while(true){

				System.out.println("服务器状态"+client.isConnected());
				send = in.readLine();
				PrintWriter out = new PrintWriter(client.getOutputStream(),true);
				out.println(send);

				if(!"byte".equals(send.trim()))
					out.println(send);
				else{
					out.println(send);
					System.exit(0);
				}
				new Thread(new TCPClient(){
					@SuppressWarnings("static-access")
					public void run(){
						try {
							BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
							System.out.println("b"+bis);
							byte[] b = new byte[1024];
							int len = 0;
							while(true){//(len=bis.read(b))!=-1
								System.out.println("服务器：" +client.getInetAddress().getLocalHost().getHostAddress()+"发来消息："+new String(b,0,len).trim());
							}
						} catch (IOException e) {
							System.err.println("连接服务器失败!");
						}
					}
				}).start();
			}
		}
		public void run() {}
	}

    /**
     * getJsonTemplate
     * @param requestDTO
     * @throws Exception
     */
    public String getJsonTemplate(RequestDTO requestDTO) throws Exception {
        String jsonTemplate = "";
        try{
            //"{ \"protocolId\":\""+val.replace(";","").split("=")[1]+"\"}";
            if(requestDTO.getProtocolIdName() != null) {
                String clazzName = ConstantUtil.PROTOBUF_FILE_NAME_WITHOUT_SUFFIX + "$" + requestDTO.getProtocolIdName().replace(";", "").split("=")[0];
                jsonTemplate = genJsonTemplate(Class.forName(clazzName)).toString();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return jsonTemplate;
    }

	}



