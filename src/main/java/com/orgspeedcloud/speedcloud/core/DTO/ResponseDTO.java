package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

/**
 * ResponseDTO是所有返回请求的格式化,由 code , msg , data 组成
 * @author Chen
 */
@Data
public class ResponseDTO {
    private Integer code;
    private String msg;
    private Object data;

    private ResponseDTO(Integer code,String msg,Object data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResponseDTO success(Object data){
        return new ResponseDTO(ResponseStatus.Success.getCode(), ResponseStatus.Success.getMsg(), data);
    }
    public static ResponseDTO notFound(Object data){
        return new ResponseDTO(ResponseStatus.Not_Found.getCode(), ResponseStatus.Not_Found.getMsg(), data);
    }
    public static ResponseDTO error(Object data){
        return new ResponseDTO(ResponseStatus.Error.getCode(), ResponseStatus.Error.getMsg(), data);
    }

    public static ResponseDTO notAuthentication() {
        return new ResponseDTO(ResponseStatus.Not_Authentication.getCode(),ResponseStatus.Not_Authentication.getMsg(),ResponseStatus.Not_Authentication.getMsg());
    }

    public static ResponseDTO banned() {
        return new ResponseDTO(ResponseStatus.Banned.getCode(), ResponseStatus.Banned.getMsg(), null);
    }
}
