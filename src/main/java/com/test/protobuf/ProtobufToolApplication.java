package com.test.protobuf;
import com.google.protobuf.Message;
import com.test.protobuf.controller.ProtobufController;
import com.test.protobuf.dto.RequestDTO;
import com.test.protobuf.dto.ResponseDTO;
import com.test.protobuf.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;


@SpringBootApplication
public class ProtobufToolApplication extends SpringBootServletInitializer  implements Callable<String> {
    private static final Logger logger = LoggerFactory.getLogger(ProtobufToolApplication.class);


	/**
	 * 支持部署到自己的tomcat
	 * @param application
	 * @return
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ProtobufToolApplication.class);
	}

	/**
	 * 加载字符过滤器
	 * @return
	 */
	@Bean
	public FilterRegistrationBean characterFilterRegistration() {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setForceEncoding(true);
		characterEncodingFilter.setEncoding("utf-8");
		return new FilterRegistrationBean(characterEncodingFilter);
	}

	public static List uidList(){
        List alist = new ArrayList();
        alist.add("0");
        alist.add("105027");
 
        return alist;
    }

	public static int getUid(int flag){

        return Integer.valueOf(uidList().get(flag).toString());
    }

    public String call() throws Exception {
        logger.info("我是线程:" + Thread.currentThread().getName());
        //测试机器人逻辑
        try {
            ApplicationContext context = SpringUtil.getApplicationContext();
            ProtobufController pc = context.getBean(ProtobufController.class);
            RequestDTO requestDTO = new RequestDTO();
            String httpParamData = "";
            String ticket = "";
            ResponseDTO responseDTO = null;
            int flag = Integer.valueOf(Thread.currentThread().getName().split("-thread-")[1]);
            int uid = getUid(flag);
            int roomId = 91;
            String socketUrlWithPort = "http://land.cn";
            String socketUrl  = "land.cn";
            String httpUrl = "http://land.cn/login";
            int socketPort  = 18880;
            //for (int i = 0; i < 10; i++) {
                //uid++;
                //登录http接口
                //初始化post请求接口数据
                httpParamData = "{\"uid\":\""+uid+"\"}";
                requestDTO.setInterfaceUrl(httpUrl);
                requestDTO.setMethodType("POST");
                requestDTO.setProtocolType("application/json");
                requestDTO.setParamData(httpParamData);
                responseDTO = pc.protobufRequest(requestDTO);
                logger.info("responseDTO.getResponseBody():"+responseDTO.getResponseBody());
                if(responseDTO.getResponseBody().length() > 0)
                    ticket = responseDTO.getResponseBody().split("ticket\":")[1].split("\"")[1];



                //初始化socket
                pc.initSocket(socketUrl, socketPort);

                //执行请求
                requestDTO = new RequestDTO();
                responseDTO = null;
                requestDTO.setInterfaceUrl(socketUrlWithPort);
                requestDTO.setProtocolType("application/x-protobuf");
                requestDTO.setMethodType("SOCKET");
                requestDTO.setProtocolIdName("5");
                requestDTO.setParamData("{\"idKey\":\"" + uid + "\"} ");
                requestDTO.setProtoFileName("All.proto");
                responseDTO = pc.protobufRequest(requestDTO);
                pc.getRadio();
                //System.out.println(responseDTO.getResponseBody());
            //logger.info("--------------" +pc.checkSocket()+ "-----------------");


                //执行请求
                requestDTO = new RequestDTO();
                responseDTO = null;
                requestDTO.setInterfaceUrl(socketUrlWithPort);
                requestDTO.setProtocolType("application/x-protobuf");
                requestDTO.setMethodType("SOCKET");
                requestDTO.setProtocolIdName("01");
                requestDTO.setParamData("{\n" +
                    "  \"ticket\": \"" + ticket + "\",\n" +
                    "  \"cliVersion\": \"\",\n" +
                    "  \"lang\": \"\",\n" +
                    "  \"baseVersion\": 0\n" +
                    "} ");
                requestDTO.setProtoFileName("AllInOneProtobufProtocol.proto");
                responseDTO = pc.protobufRequest(requestDTO);

                requestDTO = new RequestDTO();
                responseDTO = null;
                requestDTO.setInterfaceUrl(socketUrlWithPort);
                requestDTO.setProtocolType("application/x-protobuf");
                requestDTO.setMethodType("SOCKET");
                requestDTO.setProtocolIdName("04");
                requestDTO.setParamData("{\n" +
                    "  \"roomID\": " + roomId + ",\n" +
                    "  \"secretCode\": \"\",\n" +
                    "  \"invitePlayerId\": \"0\",\n" +
                    "  \"roomAliasId\": \"\"\n" +
                    "} ");
                requestDTO.setProtoFileName("All.proto");
                responseDTO = pc.protobufRequest(requestDTO);

                //logger.info(responseDTO.getResponseBody());
                //pc.getRadio();

            Message.Builder builder = AllInOneProtobufProtocol.InteractActionReq.newBuilder();
            AllInOneProtobufProtocol.ObjectInteractActionInfoData.Builder builder2 =
                (AllInOneProtobufProtocol.ObjectInteractActionInfoData.Builder)
                    builder.getFieldBuilder(builder.getDescriptorForType().findFieldByNumber(1));
            builder2.setTypeValue(1);
            builder2.setStatusValue(1);
            AllInOneProtobufProtocol.ObjectParamTo.Builder nb = null;
            nb = AllInOneProtobufProtocol.ObjectParamTo.newBuilder();
            nb.setKeyValue(1).setValue("SA3810").build();
            builder2.addParams(nb);
//            nb.setKeyValue(2).setValue("SA3811").build();
//        builder2.addParams(nb);
            //builder2.addParams(AllInOneProtobufProtocol.ObjectParamTo.newBuilder().setKeyValue(2).setValue("SA3811").build());
            logger.info(builder.toString());

                //pc.checkSocket();
                //循环执行表情动作
                //while (true) {
                requestDTO = new RequestDTO();
                responseDTO = null;
                requestDTO.setInterfaceUrl(socketUrlWithPort);
                requestDTO.setProtocolType("application/x-protobuf");
                requestDTO.setMethodType("SOCKET");
                requestDTO.setProtocolIdName("13");
                requestDTO.setParamObjectData(builder);
                requestDTO.setProtoFileName("All.proto");
                for(int i = 0 ; i < 20 ; i++) {
                    responseDTO = pc.protobufRequest(requestDTO);
                    logger.info("--------------" +i+":"+ Thread.currentThread().getName() + "-----------------");
                    Thread.sleep(3000);
                }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return "SUCCESS";
    }


	public static void main(String[] args) throws Exception {

	    SpringApplication.run(ProtobufToolApplication.class, args);

//        Message.Builder builder = AllInOneProtobufProtocol.InteractActionReq.newBuilder();
//        AllInOneProtobufProtocol.ObjectInteractActionInfoData.Builder builder2 =
//            (AllInOneProtobufProtocol.ObjectInteractActionInfoData.Builder)
//                builder.getFieldBuilder(builder.getDescriptorForType().findFieldByNumber(1));
//        builder2.setTypeValue(1);
//        builder2.setStatusValue(1);
//        AllInOneProtobufProtocol.ObjectParamTo.Builder nb = null;
//            nb = AllInOneProtobufProtocol.ObjectParamTo.newBuilder();
//            nb.setKeyValue(1).setValue("SA3810").build();
//        builder2.addParams(nb);
////            nb.setKeyValue(2).setValue("SA3811").build();
////        builder2.addParams(nb);
//        //builder2.addParams(AllInOneProtobufProtocol.ObjectParamTo.newBuilder().setKeyValue(2).setValue("SA3811").build());
//        logger.info(builder.toString());


        ProtobufToolApplication pta = new ProtobufToolApplication();
        //while (true) {
        ExecutorService threadPool1  = Executors.newFixedThreadPool(1);
        while (true)
            for(int i = 0 ; i < 1 ; i++) {
                Thread.sleep(10000);
                Future<String> threadPool1_1 = threadPool1.submit(pta);
                //logger.info(threadPool1_1.get());
            }


//            try {
//                Thread.sleep(100000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            threadPool1.shutdownNow();
        //}

    }
}
