package com.billstudy.qmkg.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class Comment {
    // e.g: 记忆碎片
    private String commentUserName;

    // e.g: https://node.kg.qq.com/personal?uid=639a9a812229338a31
    private String commentUserHomeUrl;

    // e.g: https://shp.qlogo.cn/ttsing/177474415/177474415/100
    private String commentUserImage;

    // e.g: 2017-07-06 08:13
    private String commentTime;

    // e.g: 唱的真好！
    private String commentText;


}
