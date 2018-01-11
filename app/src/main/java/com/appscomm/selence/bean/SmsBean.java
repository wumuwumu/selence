package com.appscomm.selence.bean;

/**
 * Created by Administrator on 2017/10/11.
 */

public class SmsBean {


    /**
     * tel : {"nationcode":"86","mobile":"13788888888"}
     * type : 0
     * msg : 你的验证码是1234
     * sig : ecab4881ee80ad3d76bb1da68387428ca752eb885e52621a3129dcf4d9bc4fd4
     * time : 1457336869
     * extend :
     * ext :
     */

    private TelBean tel;
    private int type;
    private String msg;
    private String sig;
    private int time;
    private String extend;
    private String ext;

    public TelBean getTel() {
        return tel;
    }

    public void setTel(TelBean tel) {
        this.tel = tel;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public static class TelBean {
        /**
         * nationcode : 86
         * mobile : 13788888888
         */

        private String nationcode;
        private String mobile;

        public String getNationcode() {
            return nationcode;
        }

        public void setNationcode(String nationcode) {
            this.nationcode = nationcode;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }
}
