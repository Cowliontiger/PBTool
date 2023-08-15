package com.test.protobuf.controller;

import com.test.protobuf.dto.RequestDTO;
import com.test.protobuf.dto.ResponseDTO;
import com.test.protobuf.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.test.protobuf.util.ConstantUtil.Dot_STRING;
import static com.test.protobuf.util.ConstantUtil.JAVA_PACKAGE_NAME;


@Controller
public class ProtobufController {

	private final Logger logger = LoggerFactory.getLogger(ProtobufController.class);

	@Autowired
	private HttpClientUtil httpClientUtil;

	@Autowired
	private ClassUtil classUtil;

	@Autowired
	private ProtoUtil protoUtil;
	private Object Socket;


	/**
	 * 加载首页
	 * @return
	 */
	@GetMapping(value = {"/", "home", "index"})
	public String index(Model model) {
	    try {
            model.addAttribute("protoFileList", ConstantUtil.protoFile);
            model.addAttribute("protocolId", ConstantUtil.protocolId);
            //todo
            //要增加git权限才可以下载最新的proto文件
            //download();
        }catch (Exception e){
	        e.printStackTrace();
        }
		return "index";
	}

	/**
	 * 上传 proto 文件
	 * @param protoFile
	 * @return
	 */
	@ResponseBody
	@PostMapping("/uploadProtoFile")
	public String uploadProtoFile(@RequestParam("protoFile") MultipartFile protoFile) {
		if (!protoFile.isEmpty()) {
			try {
				// 文件名称
				String protoFileName = protoFile.getOriginalFilename();
				// 将上传的文件名保存到 protoFile list中供前端页面选择
				ConstantUtil.protoFile.add(protoFileName);
				// 创建文件
				//File file = new File(ConstantUtil.PROTOBUF_FILE_PATH, protoFileName);
                File file = new File(protoFileName);
				protoFile.transferTo(file);

				// 将.proto文件生成java文件
				String className = protoUtil.generationJavaFile(protoFileName);
				// 动态编译java文件
                Class clazz = classUtil.dynamicLoad(className);
                String requestClassName = clazz.getName();
                logger.info("requestClassName：" + requestClassName);
			} catch (IOException e) {
				logger.error("proto文件上传失败！");
				e.printStackTrace();
				return "上传失败";
			} catch (ClassNotFoundException e) {
				logger.error("编译文件失败！");
				e.printStackTrace();
				return "编译失败";
			}
			return "上传成功";
		} else {
			return "上传失败";
		}
	}

	/**
	 * 执行请求
	 * @param requestDTO
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/executeHttpRequest")
	public ResponseDTO protobufRequest(RequestDTO requestDTO)

//            throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        throws Exception{
        Class dtoClass = null;
        String dtoClassName,protocolId = "";
        // 请求数据类型为：application/x-protobuf
        if (ConstantUtil.MEDIA_TYPE_PROTOCOL_BUFFER.equalsIgnoreCase(requestDTO.getProtocolType())) {
			System.out.println("requestDTO:"+requestDTO);
            String protoFileName = requestDTO.getProtoFileName().replace(".proto", "");
            //String dtoClassName = protoFileName.replace("Request", "");
            dtoClassName = requestDTO.getProtocolIdName().split("=")[0];
            protocolId = requestDTO.getProtocolIdName().split("=")[1];
            String requestClassName = ConstantUtil.PROTOBUF_PACKAGE_NAME + "." + protoFileName;

            if(protoFileName.equals("All")){
                requestClassName = protoFileName;
                //dtoClassName = "RandomNameReq";
            }
            logger.info("dtoClassName:"+dtoClassName+";protocolId:"+protocolId);
            // 加载内部类
            //ShopType
            try {
                dtoClass = Class.forName(JAVA_PACKAGE_NAME + Dot_STRING + requestClassName + "$" + dtoClassName);
            } catch (ClassNotFoundException c){
                logger.info(requestClassName + "$" + dtoClassName + " is not exist.");
            }
            //dtoClass = Class.forName(requestClassName + "$" + "ShopType");
        }
		// 执行请求
        ResponseDTO responseDTO = httpClientUtil.executeHttpRequest(requestDTO, dtoClass,protocolId);
		return responseDTO;
	}
	/**
	 * 连接socket
	 * @param host,port
	 * @return Socket
	 */
	@ResponseBody
	@RequestMapping ("/initSocket")
	public boolean initSocket(String host, int port) throws IOException {
		return httpClientUtil.initSocket(host,port);
	}
	/**
	 * 关闭socket
	 * @return Socket
	 */
	@ResponseBody
	@RequestMapping ("/closeSocket")
	public boolean closeSocket() throws IOException {
		return httpClientUtil.closeSocket();
	}
	/**
	 * 检查socket
	 * @return Socket
	 */
	@ResponseBody
	@RequestMapping ("/checkSocket")
	public boolean checkSocket() throws IOException {
		return httpClientUtil.checkSocket();
	}


    /**
     * 获取广播信息
     * @return Socket
     */
    @ResponseBody
    @RequestMapping ("/getRadio")
    public String getRadio() throws Exception {
        logger.info("getradio接口");
        String retSting=httpClientUtil.getRadio();
        System.out.println("retSting"+retSting);

        return retSting;
    }

    /**
     * getJsonTemplate
     * @return String
     */
    @ResponseBody
    @RequestMapping ("/getJsonTemplate")
    public String getJsonTemplate(RequestDTO requestDT) throws Exception {
        return httpClientUtil.getJsonTemplate(requestDT);
    }

    /**
     * 下载git上面的protobuf最新文件
     * @param
     * @return
     */
    @ResponseBody
    @GetMapping("/download")
    public Boolean download()
        throws Exception {
        Boolean isOk = false;
        logger.info("开始下载proto文件");
        //DownloadUtil.downloadFile(ConstantUtil.PROTOBUF_FILE_PATH_FROM_GIT,ConstantUtil.PROTOBUF_FILE_NAME_FROM_GIT,response,request);

        File file = DownloadUtil.saveUrlAs(ConstantUtil.PROTOBUF_FILE_PATH_FROM_GIT+ConstantUtil.PROTOBUF_FILE_NAME_FROM_GIT,
            ConstantUtil.PROTOBUF_FILE_PATH+"//"+ConstantUtil.PROTOBUF_FILE_NAME_FROM_GIT,"GET");

        logger.info("下载完成proto文件");
        isOk = true;
        return isOk;
    }
}
