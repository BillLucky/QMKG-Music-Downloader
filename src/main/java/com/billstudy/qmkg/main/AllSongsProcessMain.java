package com.billstudy.qmkg.main;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.billstudy.qmkg.model.Song;
import lombok.SneakyThrows;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 此类用于加工提取所有的歌信息，并存到JSON里面去
 */
public class AllSongsProcessMain {
    @SneakyThrows
    public static void main(String[] args) {
        List<Song> songs = new ArrayList<>();
        Document document = Jsoup.parse(FileUtils.getFile("/Users/libiao/Documents/Code/QMKG-Music-Downloader/src/main/resources/all_song_pages.html"));
        Elements songLis = document.getElementsByClass("mod_graph__item");
        for (int i = 0; i < songLis.size(); i++) {
            Element liEle = songLis.get(i);
            Elements listenedAndTalkTimes = liEle.getElementsByClass("mod_graph__num");
            String dataId = liEle.attr("data-id");
            songs.add(
                    Song.builder()
                            .dataId(dataId)
                            .songName(liEle.getElementsByClass("mod_graph__desc").get(0).text())
                            .songPlayTimes(Integer.parseInt(listenedAndTalkTimes.get(0).text()))
                            .songTalkTimes(Integer.parseInt(listenedAndTalkTimes.get(1).text()))
                            .songHomeUrl(liEle.getElementsByClass("mod_graph__img").get(0).attr("src"))
                            .SongNo(i+1)
                            .songDetailAccessUrl("https://kg.qq.com/node/play?s=" + dataId)
                            .build());
        }

        System.out.println(JSONObject.of("songs", songs).toString(JSONWriter.Feature.PrettyFormat));
    }
}
