package com.billstudy.qmkg.model;

import com.alibaba.fastjson2.JSONObject;
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

    // e.g: http://tx.stream.kg.qq.com/njc-kgsvp/86_s_0bc3lxfycciafmadour37btl6xodqfeqa5ca.f0.mp4?vkey=62A93215A340B0629B1BC7116CFEB257BEA845FBA913CACFAD443343008AC89F848908E3909F26470C394DFBC94E26629344BE287F59B31979A15370C7940B7644C6801E7489F922628DFDAF43F8F5010F9817E3EA97DE15&dis_k=2b0f9cd17f25efe904711f4246c91363&dis_t=1715087871&fromtag=86&ugcid=164361492_1589934543_948&nr=1
    private String songDownloadMediaUrl;

    // mp4 or m4a，根据页面的tag定，video 或者 audio
    private String mediaType;

    // 页面上的大JS对象，全存起来，后续用就不用再解析了
    private JSONObject songDetailPageJsVarData;

    public String generatorSongOutputName() {
        return (getSongNo() + "." + songName + "-" + singerTime)
                .replaceAll(":","-")
                .replaceAll(" ", "-")
                ;

    }
}

