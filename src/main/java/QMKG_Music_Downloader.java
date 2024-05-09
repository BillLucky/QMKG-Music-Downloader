import org.apache.commons.io.ThreadUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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

        ExecutorService downloadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            private Integer threadNum = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread("thread-download-" + ++threadNum );
            }
        });

        downloadExecutor.submit(new Runnable() {
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

                System.out.println(userHomePageDocument);
                System.out.println("==============");
                System.out.println(userHomePageDocument.getElementById("player"));

                List<String> userSongsList = getSongsListByUserInfo(userHomePageDocument);
                totalSize.getAndAdd(userSongsList.size());

                // 2. 下载每首歌，解析关键数据到本地（音乐、封面、评论、日期、等关键信息）
                for (int i = 0; i < userSongsList.size(); i++) {
                    try {
                        Document userSongPage = Jsoup.connect(userSongsList.get(i)).headers(HEADERS).get();
                        DownloadSongInformationByPage(userSongPage);
                        currentSize.getAndAdd(1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });


        // 3. 同步下载进度情况
        Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread("thread-show-current-process");
            }
        }).submit(new Runnable() {
            @Override
            public void run() {
                while (true){
                    int cSize = currentSize.get();
                    int tSize = totalSize.get();
                    if (cSize < tSize){
                        System.out.println("Download process: " + cSize + " / " + tSize);
                    }
                    try {
                        ThreadUtils.sleep(Duration.ofSeconds(1));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });




    }


    /**
     * 下载歌曲信息
     * @param userSongPage 歌曲详情页
     */
    private static void DownloadSongInformationByPage(Document userSongPage) {
    }

    /**
     * 获取用户全部的歌曲清单
     * @param userHomePageDocument 用户首页地址信息
     * @return 歌曲地址清单
     */
    private static List<String> getSongsListByUserInfo(Document userHomePageDocument) {
        Elements modPlaylist__box = userHomePageDocument.getElementsByClass("mod_playlist__box");
        for (int i = 0; i < modPlaylist__box.size(); i++) {
            System.out.println(modPlaylist__box.getFirst());
        }
        return List.of();
    }
}
