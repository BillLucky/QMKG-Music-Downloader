package com.demo.arxiv.demoarxiv;


import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JsonbTester;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArxivDownloadPDFTest {


    /**
     * 明确想要的学科类目白名单
     * 用于后面做信息筛选
     */
    private final List<String> CLASS_WHITE_LIST = Arrays.asList("Physics",
            "Mathematics",
            "Computer Science",
            "Quantitative Biology",
            "Quantitative Finance",
            "Statistics",
            "Electrical Engineering and Systems Science",
            "Economics");


    /**
     * 实际的子类目标签启示位置
     * 在它之后的内容是有效内容
     */
    private final int ACTUAL_SUBCLASS_TAG_A_NUBER = 5;

    /**
     * 本地下载的文件夹路径
     */
    private final String DOWNLOAD_LOCAL_DIR_PATH = "/Users/libiao/Documents/Code/demo-arxiv/download-resources/demo";


    /**
     * 下载 Top N 的文件
     */
    private final int DOWNLOAD_FILE_TOP_N = 3;

    /**
     * arxiv官网地址
     */
    private final String ARXIV_SITE_URL = "https://arxiv.org";


    /**
     * 访问网站的headers，降低被block的概率
     */
    private final Map<String, String> DEFAULT_ACCESS_SITE_HEADERS = Map.of(
            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
            "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
    );

    /**
     * 下载特定类目下的文件
     */
    @Test
    void testDownloadSpecSubClassPdf() throws IOException {
        // 1. 读取网页，获取分类
        Document document = Jsoup
                .connect(ARXIV_SITE_URL)
                .headers(DEFAULT_ACCESS_SITE_HEADERS)
                .get();

        // 2. 读取特定类目下，如计算机科学下所有的子分类
        Elements h2Elements = document.getElementsByTag("h2");
        String targetClassName = "Computer Science";
        h2Elements.stream().iterator().forEachRemaining(ele -> {
            // 判断下是否是：我们需要的下级类目
            if (targetClassName.equals(ele.text())){
                Element ulEle = ele.nextElementSibling();
                Elements lisChildren = ulEle.children();
                lisChildren.forEach(li -> {
                    Elements aTagList = li.getElementsByTag("a");
                    int tagASize = aTagList.size();
                    if (tagASize >= ACTUAL_SUBCLASS_TAG_A_NUBER){
                        for (int i = ACTUAL_SUBCLASS_TAG_A_NUBER; i < tagASize; i++) {
                            Element a = aTagList.get(i);
                            int subClassOrder = (i - ACTUAL_SUBCLASS_TAG_A_NUBER + 1);
                            String subClassName = a.text();
                            String subClassHref = a.attr("href");
                            String subClassDirNameSuffix = "No." + subClassOrder + " - " + subClassName;

                            System.out.print(subClassDirNameSuffix + ":");
                            System.out.print(subClassName + " -> ");
                            System.out.println(subClassHref);

                            downloadFileBySubClassDirNameAndURL(subClassDirNameSuffix, subClassHref);

                        }
                    }
                });
            }
        });



//        System.out.println(document);




        // 3. 访问每个子分类的网页，并下载前三个PDF文档，到特定目录


    }

    /**
     * 下载PDF文件
     * @param subClassDirNameSuffix 子类目的后缀名
     * @param subClassHref 地址
     */
    @SneakyThrows
    private void downloadFileBySubClassDirNameAndURL(String subClassDirNameSuffix, String subClassHref) {
        File targetDownloadDir = new File(DOWNLOAD_LOCAL_DIR_PATH + "/" + subClassHref);
        if (!targetDownloadDir.exists()){
            System.out.println("mkdir dir result: " + targetDownloadDir.mkdirs());
        }

        // 访问子类目
        Document subClassDoc = Jsoup.connect(ARXIV_SITE_URL + subClassHref)
                .headers(DEFAULT_ACCESS_SITE_HEADERS)
                .get();

        Elements paperSpanTagList = subClassDoc.getElementsByClass("list-identifier");
        if (!paperSpanTagList.isEmpty()){
            for (int i = 0; i < paperSpanTagList.size(); i++) {
                Element spanTag = paperSpanTagList.get(i);
                // 获取论文名称
                // TODO：待通过此元素获取标题，后续再说
                Elements elements = spanTag.parent().select("list-title mathjax");


                // 获取PDF的下载地址
                Elements aTagList = spanTag.getElementsByTag("a");
                if (!aTagList.isEmpty()) {
                    // 排序第2个是PDF的超链接标签，如：<a href="/pdf/2402.04232" title="Download PDF">pdf</a>
                    Element aPDFTag = aTagList.get(1);
                    String pdfDownloadURLSuffix = aPDFTag.attr("href");
                }

            }
        }
        Element pdfATag = paperSpanTagList.get(0).getElementsByTag("a").get(1);
    }

    /**
     * 给特定文件夹下所有的PDF文件做    void testPatchPdfAbstractTextToSelfDir(){
     *         // 1. 读取给定的目录下的所有PDF
     *
     *         // 2. 解析每个PDF内部的abstract 文本
     *
     *         // 3. 将文本输出到以该PDF名称一致的 txt 文件中
     *
     *     }abstract抽取
     * 并输出同样名称的txt文件，在PDF所在的目录
     */


}
