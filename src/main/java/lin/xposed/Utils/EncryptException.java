package lin.xposed.Utils;

public class EncryptException extends Exception {
    private String code;
    private String msg;


    public EncryptException(String code, Exception exception) {
        this.code = code;
        this.msg = exception.getMessage();
    }

    public EncryptException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
