package com.billstudy.qmkg.model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class Song {

    // 首页信息
    // --------------------
    // 歌曲Id
    private String dataId;

    // 歌曲名称
    private String songName;

    // 歌曲首页地址
    private String songHomeUrl;

    // 歌曲发布时间和手机
    private String singerTime;

    // 歌曲播放次数
    private Integer songPlayTimes;

    // 歌曲评论次数
    private Integer songTalkTimes;

    // 详情页信息
    private String singer_say__cut;

    // 评论条目
    private List<Comment> commentList;



    // 补充信息，便于解析时用
    // --------------------

    private Integer SongNo;

    // e.g: https://kg.qq.com/node/play?s=pv2GjjpUyYJqTpaz
    private String songDetailAccessUrl;
}

