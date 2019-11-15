package com.xxx.xcloud.module.ceph.model;

import com.amazonaws.services.s3.model.CannedAccessControlList;

/**
 * 
 * @ClassName: AccessControlListEnum
 * @Description: 读写权限
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
public enum AccessControlListEnum {

    /**
     * 私有
     */
    Private(CannedAccessControlList.Private, "private"),
    /**
     * 公有读
     */
    PublicRead(CannedAccessControlList.PublicRead, "publicread"),
    /**
     * 公有读写
     */
    PublicReadWrite(CannedAccessControlList.PublicReadWrite, "publicreadwrite");

    public CannedAccessControlList acl;

    public String val;

    private AccessControlListEnum(CannedAccessControlList acl, String val) {
        this.acl = acl;
        this.val = val;
    }

    public static AccessControlListEnum getAcl(String val) {
        if (val != null) {
            if (val.equals(AccessControlListEnum.Private.val)) {
                return AccessControlListEnum.Private;
            } else if (val.equals(AccessControlListEnum.PublicRead.val)) {
                return AccessControlListEnum.PublicRead;
            } else if (val.equals(AccessControlListEnum.PublicReadWrite.val)) {
                return AccessControlListEnum.PublicReadWrite;
            }
        }

        return null;
    }

}
