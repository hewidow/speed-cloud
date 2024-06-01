package com.orgspeedcloud.speedcloud.core.handler;

import com.orgspeedcloud.speedcloud.core.DTO.ResponseDTO;
import com.orgspeedcloud.speedcloud.core.aop.authentication.AuthorizationException;
import com.orgspeedcloud.speedcloud.core.aop.authentication.BannedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常返回
 * @author Chen
 */

@ControllerAdvice
@RestController
@Slf4j
public class ExceptionController{

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity handleToken(AuthorizationException ex){
        return new ResponseEntity<Object>(ResponseDTO.notAuthentication(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(BannedException.class)
    public ResponseDTO handleBan(){
        return ResponseDTO.banned();
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseDTO noHandlerFoundHandle(NoHandlerFoundException ex){
        log.error("handle Exception:",ex);
        return ResponseDTO.notFound(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception ex){
        log.error("handle Exception:",ex);
        return new ResponseEntity<>(ResponseDTO.error(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
