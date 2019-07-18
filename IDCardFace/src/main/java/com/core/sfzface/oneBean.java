package com.core.sfzface;

/**
 * 作者：李阳
 * 时间：2019/4/18
 * 描述：人证核验bean
 */
public class oneBean {


    /**
     * 比对时间
     */
    private String comparisonTime;

    /**
     * 是否比对成功
     */
    private boolean isSuccess;

    /**
     * 身份证信息
     */
    private People people;


    /**
     * 现场采集的人脸模板，非必要，如果可以，每次人证核验成功后可以把之前采集的这个数据覆盖掉，原因：人脸会变化，胖瘦等等
     * template = new byte[8 * 1024];
     */
    private byte[] collectionTemplate;


    /**
     * sfz人脸模板,非必要
     * template = new byte[8 * 1024];
     */
    private byte[] sfzTemplate;

}
