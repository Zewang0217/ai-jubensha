package org.jubensha.aijubenshabackend.core.exception;

/**
 * 应用程序异常类
 */
public class AppException extends RuntimeException {
    
    private int status;
    private String code;
    
    public AppException(String message) {
        super(message);
        this.status = 500;
        this.code = "ERROR";
    }
    
    public AppException(int status, String message) {
        super(message);
        this.status = status;
        this.code = "ERROR";
    }
    
    public AppException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getCode() {
        return code;
    }
}
