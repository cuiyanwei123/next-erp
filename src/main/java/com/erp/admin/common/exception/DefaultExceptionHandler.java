package com.erp.admin.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.erp.admin.common.Constants;

/**
 * 
 * @author
 * @version : 1.00
 * @Description : 类概要： 全局异常处理类
 * @History：Editor version Time Operation Description*
 *
 */
@RestControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {


    private static Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /**
     * 
     * @param req
     * @param ex
     * @return
     * @about version ：1.00
     * @auther :
     * @Description ：抛出的异常统一在此处理
     */
    @ExceptionHandler
    public Map<String, Object> defaultErrorHandler(HttpServletRequest req, Exception ex) {
        // 日志记录异常message和stacktrace
        Map<String, Object> map = new HashMap<String, Object>();
        // APIException 类型自定义异常
        if (ex instanceof CommonException) {
        	CommonException e = (CommonException) ex;
            map.put("ret", e.getErrorCode());
            map.put("msg", e.getErrorMsg());
        } else {
            // 其他未知异常
            log.error("系统异常", ex);
            map.put("ret", Constants.RET_UNKNOW_ERROR);
            map.put("msg",ex.getMessage());
        }
        return map;
    }

    public static String getErrorInfoFromException(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return "bad getErrorInfoFromException";
        }
    }

    /**
     * 
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     * @about version ：1.00
     * @author :
     * @Description ：处理404错误
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ret", HttpStatus.NOT_FOUND.value());
        map.put("msg", HttpStatus.NOT_FOUND.name());
        log.error("ret:{},msg:{}", HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name());
        return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
    }

    /**
     * 
     * @param ex
     * @param body
     * @param headers
     * @param status
     * @param request
     * @return
     * @about version ：1.00
     * @author :
     * @Description ：处理 405 错误
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            // 405
            map.put("ret", HttpStatus.METHOD_NOT_ALLOWED.value());
            map.put("msg", HttpStatus.METHOD_NOT_ALLOWED.name());
            log.error("ret:{},msg:{}", HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.name());
        }
        return new ResponseEntity<>(map, status);
    }

}