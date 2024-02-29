package cn.moon.base;


import lombok.Data;

/**
 * 操作消息提醒
 */

@Data
public class AjaxResult  {


    boolean success;

    String msg;
    Object data;

    int code;



    public AjaxResult(boolean success, String msg, Object data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public static AjaxResult success() {
        return AjaxResult.success("操作成功",null);
    }



    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(true, msg, data);
    }


    public static AjaxResult error(String msg) {
        return new AjaxResult(false, msg, null);
    }


    public static AjaxResult success(String msg) {
        return new AjaxResult(true, msg, null);
    }
}
