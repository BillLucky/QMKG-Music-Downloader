package com.billstudy.qmkg.main;
import lombok.SneakyThrows;
import org.apache.commons.io.ThreadUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QMKG_Music_Downloader {

    private static volatile AtomicInteger totalSize = new AtomicInteger();

    private static volatile AtomicInteger currentSize = new AtomicInteger();

    // 访问网站的headers，降低被封杀的可能性
    private static final Map<String, String> HEADERS = Map.of(
            "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
    );

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                String userHomePageUrl = "https://kg.qq.com/node/personal?uid=639b9986232c338236";
                // 1. 访问到用户的歌曲清单（作品清单）

                Document userHomePageDocument = null;
                try {
                    userHomePageDocument = Jsoup.connect(userHomePageUrl)
                            .headers(HEADERS).get();
                } catch (IOException e) {
                    throw new RuntimeException("Accept the user home url error.", e);
                }

                List<String> userSongsList = getSongsListByUserInfo(userHomePageDocument);
                totalSize.getAndAdd(userSongsList.size());

                // 2. 下载每首歌，解析关键数据到本地（音乐、封面、评论、日期、等关键信息）
                for (int i = 0; i < userSongsList.size(); i++) {
                    DownloadSongInformationByPageUrl(userSongsList.get(i));
                    currentSize.getAndAdd(1);
                }
            }
        }.start();


        // 3. 同步下载进度情况

        new Thread("thread-download-process") {

            @SneakyThrows
            @Override
            public void run() {
                // 让上面的线程先跑起来
                ThreadUtils.sleep(Duration.ofSeconds(3));

                while (true){
                    int cSize = currentSize.get();
                    int tSize = totalSize.get();
                    if (cSize >= tSize){
                        break;
                    }
                    System.out.println("Download process: " + cSize + " / " + tSize);
                    ThreadUtils.sleep(Duration.ofSeconds(1));
                }
            }
        }.start();


    }


    /**
     * 下载歌曲信息
     * @param userSongPageUrl 歌曲详情页
     */
    @SneakyThrows
    private static void DownloadSongInformationByPageUrl(String userSongPageUrl) {
//        Document userSongPage = Jsoup.connect(userSongPageUrl).headers(HEADERS).get();

        System.out.println("mock download song: " + userSongPageUrl);
    }

    /**
     * 获取用户全部的歌曲清单
     * @param userHomePageDocument 用户首页地址信息
     * @return 歌曲地址清单
     */
    private static List<String> getSongsListByUserInfo(Document userHomePageDocument) {
        List<String> songUrlList = new ArrayList<>(500);
        Elements modPlaylist__box = userHomePageDocument.getElementsByClass("mod_playlist__box");
        for (int i = 0; i < modPlaylist__box.size(); i++) {
            // e.g: <a href="javascript:;" data-playurl="https://node.kg.qq.com/qQYHup9ZjM/play_v2/?s=sBFqnnsD4iLGIsrt&amp;g_f=personal&amp;appsource=&amp;pageId=personalH5" class="mod_playlist__cover j_go_album" data-luopanv2="267,267002,267002004" data-hc_type="0"> <img class="play_img" src="//y.gtimg.cn/mediastyle/global/kg/default/default_cd.png?max_age=2592000" data-lazyload="http://pic.kg.qq.com/ttkg/0/a4a75eec01e8ccb2047e47ef83f9fafc5981bfae/320?bj=aHR0cHM6Ly95Lmd0aW1nLmNuL211c2ljL3Bob3RvX25ldy9UMDAyUjMwMHgzMDBNMDAwMDAzZE5ieDQxUzlldGYuanBnP21heF9hZ2U9MjU5MjAwMCszQTk2NkY5OEM3ODFBRTMxQUUxNDc2RjY5NzFFQzhDOQ%3D%3D" alt="王姐 湘女多情"> <i class="black_mask"></i> <i class="icon_play_m"></i> </a>
            Element aTagElement = modPlaylist__box.get(i).children().getFirst();
            String dataPlayUrl = aTagElement.attr("data-playurl");
            songUrlList.add(dataPlayUrl);
        }
        return songUrlList;
    }
}
