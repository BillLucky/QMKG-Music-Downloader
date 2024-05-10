# 背景
用于下载全民K歌的歌曲，便于做信息备份

# 功能说明
1. 支持对指定用户公开的作品进行下载，包含m4a、mp4
2. 支持对每首歌的封面、相册、讨论信息等下载，部分是以文件、部分是以JSON存在的
   - ![01.歌曲解析说明.png](https://github.com/BillLucky/QMKG-Music-Downloader/blob/main/src/main/resources/images/01.%E6%AD%8C%E6%9B%B2%E8%A7%A3%E6%9E%90%E8%AF%B4%E6%98%8E.png)
3. 以上的内容是以独立的文件保存的，按需更换代码就行（如果未来没有来重构这部分代码的话）

# 使用说明
1. 获取用户全部的歌单
   - 这个步骤，可以用Chrome开启手机模式，然后滚动全部歌曲之后
   - 在Element里，copy整个页面到：[all_song_pages.html](https://github.com/BillLucky/QMKG-Music-Downloader/blob/main/src/main/resources/all_song_pages.html)
      - 之所以这么干，是因为不想再去调用K歌的API获取列表了
      - 也只有在移动版上，滚动之后才能获取到全部歌，见：![02.获取全部的歌单方式，手机模式滚动页面，之后copy html 页面到文件.jpg](https://github.com/BillLucky/QMKG-Music-Downloader/blob/main/src/main/resources/images/02.%E8%8E%B7%E5%8F%96%E5%85%A8%E9%83%A8%E7%9A%84%E6%AD%8C%E5%8D%95%E6%96%B9%E5%BC%8F%EF%BC%8C%E6%89%8B%E6%9C%BA%E6%A8%A1%E5%BC%8F%E6%BB%9A%E5%8A%A8%E9%A1%B5%E9%9D%A2%EF%BC%8C%E4%B9%8B%E5%90%8Ecopy%20html%20%E9%A1%B5%E9%9D%A2%E5%88%B0%E6%96%87%E4%BB%B6.jpg)
      - 软件默认是不让下载的，付费好像能下载，但是不知道能不能导出
2. 解析html页面，获取网站的歌曲信息，输出到[JSON文件: 01.origin_songs.json](https://github.com/BillLucky/QMKG-Music-Downloader/blob/main/src/main/resources/json/01.origin_songs.json)
3. 对JSON文件进行逐条解析，下载每首歌到本地
    - 最终成果物说明如下图：![03. 最终成果物的输出说明.jpg](https://github.com/BillLucky/QMKG-Music-Downloader/blob/main/src/main/resources/images/03.%20%E6%9C%80%E7%BB%88%E6%88%90%E6%9E%9C%E7%89%A9%E7%9A%84%E8%BE%93%E5%87%BA%E8%AF%B4%E6%98%8E.jpg)

---

🥰  enjoy it!

---

> 初心：
> - 保留备份故去老母亲的作品，尽管她唱的可能不好，但是在我们对我们子女来说，每一段音像都格外地弥足珍贵。
> - 听到这些声音后，内心既开心又难过，五味杂陈。
>   - 我把老妈常唱的：阿哥阿妹，单曲循环听了不下100遍
> - 开心的点：在我们不在家时，她虽身患绝症但是怡然自乐，也能从音乐里感受到她的快乐和坚强。
> - 难过的点：原因和上面一样，心里非常心疼她，愿天堂没有病痛。也激励我辈 加倍奋发努力 让身边人过的更好！

> 以上，2024.05.10 11:54 于 北京 程远公寓