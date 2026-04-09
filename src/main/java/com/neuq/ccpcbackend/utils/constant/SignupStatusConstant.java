package com.neuq.ccpcbackend.utils.constant;

import lombok.Data;

@Data
public class SignupStatusConstant {

    public static final int SIGNED_UP = 1;                      // 已报名
    public static final int REVIEW_PASSED = 2;                  // 审核通过
    public static final int PAYMENT_UPLOADED = 3;               // 已上传缴费凭证，待审核
    public static final int PAYMENT_REVIEW_PASSED = 4;          // 审核通过，报名成功
    public static final int SIGNIN_SUCCESS = 5;                 // 现场签到成
    public static final int PHOTO_UPLOADED = 6;                 // 照片上传成功
    public static final int RESULT_UPLOADED = 7;               // 比赛结果上传成功
    public static final int EXPERIENCE_ENDED = 8;              // 比赛经历结束

    private static final int SIGNUP_REFUSED = 9;                //报名审核不通过
    private static final int PAYMENT_REFUSED = 10;               //缴费审核不通过
    private static final int SIGNUP_CANCELLED = 11;              // 报名取消

    private SignupStatusConstant() {
        // 防止实例化
    }
}

