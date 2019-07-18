package com.core.sfzface;

/**
 * 作者：李阳
 * 时间：2019/4/18
 * 描述：
 */
public class twoBean {


    /**
     * 采集时间
     */
    private String AcquisitionTime;


       /**
     * 现场采集的人脸模板，非必要，如果可以，每次人证核验成功后可以把之前采集的这个数据覆盖掉，原因：人脸会变化，胖瘦等等
     * template = new byte[8 * 1024];
     */
    private byte[] collectionTemplate;


    /**
     * 考勤卡号
     */
    private String id;

}
