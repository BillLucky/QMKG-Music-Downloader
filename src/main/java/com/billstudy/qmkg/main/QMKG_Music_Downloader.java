package com.billstudy.qmkg.main;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.billstudy.qmkg.model.Song;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.ThreadUtils;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QMKG_Music_Downloader {

    private static volatile AtomicInteger totalSize = new AtomicInteger();

    private static volatile AtomicInteger currentSize = new AtomicInteger();

    private static final String MUSIC_OUTPUT_DIR = "/Users/libiao/Documents/Code/QMKG-Music-Downloader/src/main/resources/output/musics/";

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 访问网站的headers，降低被封杀的可能性
    private static final Map<String, String> HEADERS = Map.of(
            "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
    );

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                // 1. 访问到用户的歌曲清单（作品清单）

                List<Song> userSongsList = getSongsListByUserInfo();
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

                while (true) {
                    int cSize = currentSize.get();
                    int tSize = totalSize.get();
                    if (cSize >= tSize) {
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
     *
     * @param song 歌曲
     */
    @SneakyThrows
    private static void DownloadSongInformationByPageUrl(Song song) {
        int maxRetires = 10;

        // 下载歌曲
        Document songDetailPage = null;
        for (int i = 0; i <= maxRetires; i++ ){
            if(i > 0){
                System.out.println("重试第" + i + "次，下载：" + song.getSongName());
            }

            try{
                songDetailPage = Jsoup.connect(song.getSongDetailAccessUrl())
                        .timeout(5000)
                        .headers(HEADERS).get();
                break;
            }catch (IOException e){
                System.err.println("访问歌曲详情页报错：" + song.getSongName() + ", error msg:" + e.toString());
            }

        }

        // 重试策略之后，还是不行，就跳过这条，先处理后面的
        if (songDetailPage == null){
            System.out.println("重试下载失败，跳过处理：" + song.generatorSongOutputName());
            return;
        }

        song.setSinger_say__cut(songDetailPage.getElementsByClass("singer_say__cut").get(0).text());
        song.setSingerTime(songDetailPage.getElementsByClass("singer_time").get(0).text());

        // 获取javascript里面的变量定义：window.__DATA__ = {"shareid":....
        String javascriptData = songDetailPage.getElementsByTag("script").get(2).html().split("window.__DATA__ = ")[1];

        // 去除最后一个字符，是个分号 ; 准备用于解析内容
        String prettyJsonData = javascriptData.substring(0, javascriptData.length() - 1);

        JSONObject json = JSONObject.parse(prettyJsonData);
        JSONObject detail = json.getJSONObject("detail");

        // 如果歌的地址是空的，就取mp4的地址
        String downloadMediaUrl = detail.getString("playurl");
        song.setMediaType("m4a");
        if (StringUtil.isBlank(downloadMediaUrl)) {
            downloadMediaUrl = detail.getString("playurl_video");
            song.setMediaType("mp4");
        }
        song.setSongDownloadMediaUrl(downloadMediaUrl);
        song.setSongDetailPageJsVarData(json);

        System.out.println("download song: " + song.getSongDownloadMediaUrl() + "   " + song.getMediaType());

        String songOutputName = song.generatorSongOutputName();
        String songDirName = MUSIC_OUTPUT_DIR + songOutputName;

        // 考虑断点续传，如果之前已经下载过，就跳过这条记录，根据是否有done.txt文件来判断
        File haveDoneFile = FileUtils.getFile(songDirName + "/" + "done.txt");
        if(haveDoneFile.exists()){
            // 已经下载过了，直接跳过
            System.out.println("已下载，跳过这首歌：" + songOutputName);
            return;
        }


        File tarDirName = new File(songDirName);
        if (Files.deleteIfExists(tarDirName.toPath())) {
            FileUtils.createParentDirectories(tarDirName);
        }

        FileUtils.writeStringToFile(
                FileUtils.getFile(songDirName + "/" + "song-details.json")
                ,JSONObject.toJSONString(song, JSONWriter.Feature.PrettyFormat)
        );

        // 下载歌曲、视频
        downloadFile(song.getSongDownloadMediaUrl(),
                songDirName + "/" + songOutputName
                + ("m4a".equals(song.getMediaType()) ? ".m4a" : ".mp4")
        );

        // 下载封面
        downloadFile(song.getSongHomeUrl(),
                songDirName + "/" + "home.jpeg"
        );

        // 下载相册，可能有多张图
        downloadAlbum(json.getJSONArray("album"),songDirName);

        // 记录下载完成标记，用于标记断点续传 和 下载完成时间
        FileUtils.writeStringToFile(
                haveDoneFile,
                "done by " + dateFormat.format(new Date()));
    }

    @SneakyThrows
    private static void downloadAlbum(JSONArray album, String songDirName) {
        if (album == null || album.size() == 0) {
            return;
        }

        String albumOutputDirName = songDirName + "/album/";
        File albumOutputDir = new File(albumOutputDirName);
        Files.createDirectories(albumOutputDir.toPath());

        for (int i = 0; i < album.size(); i++) {
            downloadFile(album.getString(i), albumOutputDirName + i + ".jpeg");
        }
    }

    /**
     * 获取用户全部的歌曲清单
     *
     * @return 歌曲信息
     */
    @SneakyThrows
    private static List<Song> getSongsListByUserInfo() {
        JSONObject songsJson = JSONObject.parse(FileUtils.readFileToString(FileUtils.getFile("/Users/libiao/Documents/Code/QMKG-Music-Downloader/src/main/resources/json/01.origin_songs.json")));
        List<Song> songs = songsJson.getJSONArray("songs").toJavaList(Song.class);
        return songs;
    }

    public static void downloadFile(String url, String destinationFile) {
        try (InputStream inputStream = new URL(url).openStream();
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            System.out.println("Download completed: " + destinationFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
