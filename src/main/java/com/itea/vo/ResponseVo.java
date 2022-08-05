package com.itea.vo;

/**
 * @description:发送给client的信息
 * @author: oyyy
 * @date: 2022/8/3 18:41
 */
public class ResponseVo {
    String msg;

    public ResponseVo(String msg) {
        this.msg = msg;
    }

    public ResponseVo() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResponseVo{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
