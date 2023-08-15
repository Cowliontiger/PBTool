package com.test.protobuf.dto;

public class ResponseDTO {

	/**
	 * 请求头信息
	 */
	private String requestHeader;

	/**
	 * 请求参数信息
	 */
	private String requestBody;

	/**
	 * 响应头信息
	 */
	private String responseHeader;

	/**
	 * 响应参数信息
	 */
	private String responseBody;

	public String getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(String requestHeader) {
		this.requestHeader = requestHeader;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public String getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(String responseHeader) {
		this.responseHeader = responseHeader;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	@Override
	public String toString() {
		return "ResponseDTO{" +
				"requestHeader='" + requestHeader + '\'' +
				", requestBody='" + requestBody + '\'' +
				", responseHeader='" + responseHeader + '\'' +
				", responseBody='" + responseBody + '\'' +
				'}';
	}
}
