package com.demo.arxiv.demoarxiv;


import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ArxivTest {

    /**
     * 测试去访问arxiv网站，验证网站的连通性
     */
    @Test
    void testAccessArxivPageByURL(){
        try {
            Document axrivPage = Jsoup.connect("https://arxiv.org/").get();
            System.out.println("title: " + axrivPage.title());
            System.out.println("------------");
            System.out.println(axrivPage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试下载一个具体的文件
     */
    @Test
    void testDownloadPDFByURL() throws IOException {
        var pdfURL = "https://arxiv.org/pdf/2402.02643.pdf";
        var downaloadDir = "/Users/libiao/Documents/Code/demo-arxiv/download-resources";
        String fileName = downloadFileToDirByURL(pdfURL,downaloadDir);
        System.out.println(fileName);
    }


    /**
     * 测试分析PDF文件的内容
     */
    @Test
    void testAnalysisPDFStruct() throws IOException {
        // 1. 读取本地的一个PDF文件
        String fileName = "/Users/libiao/Documents/Code/demo-arxiv/download-resources/2402.02643.pdf";
//        String fileName = "/Users/libiao/Documents/Code/demo-arxiv/download-resources/2402.04232.pdf";
        File pdfFile = new File(fileName);

        // 2. 解析PDF文件（找外部工具库）- 实现的效果：按需读PDF内部的各个结构和内容
//        PDDocument document = null;
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFText2HTML pdfText2HTML = new PDFText2HTML();
            pdfText2HTML.setStartPage(1);
            pdfText2HTML.setEndPage(7);
            String text = pdfText2HTML.getText(document);

            System.out.println(text);
            System.out.println("-----------------");
            Document parseHtmlDoc = Jsoup.parse(text);

            Elements anAbstractEles = parseHtmlDoc.getElementsMatchingOwnText("ABSTRACT");
            if (anAbstractEles.size() >= 1) {
                String abstractText = anAbstractEles.get(0).nextElementSibling().text();
                System.out.println("Abstract Text:" + abstractText);
            }
        }

    }


    /**
     * 下载文件到指定的文件夹下
     * @param fileUrl 文件地址
     * @param downaloadDir 目标文件夹
     * @return 最终的文件名称
     */
    private String downloadFileToDirByURL(String fileUrl, String downaloadDir) throws IOException {
        String fileName = "2402.02643.pdf";
        String finalFileName = downaloadDir + "/" + fileName;

        // 1. 访问目标的PDF，拿到响应，并写到本地
        FileUtils.copyURLToFile(new URL(fileUrl), new File(finalFileName));
        return finalFileName;
    }


    /**
     * This will print the documents data to System.out.
     *
     * @param document The document to get the metadata from.
     *
     * @throws IOException If there is an error getting the page count.
     */
    public void printMetadata( PDDocument document ) throws IOException
    {
        PDDocumentInformation info = document.getDocumentInformation();
        PDDocumentCatalog cat = document.getDocumentCatalog();
        PDMetadata metadata = cat.getMetadata();
        System.out.println( "Page Count=" + document.getNumberOfPages() );
        System.out.println( "Title=" + info.getTitle() );
        System.out.println( "Author=" + info.getAuthor() );
        System.out.println( "Subject=" + info.getSubject() );
        System.out.println( "Keywords=" + info.getKeywords() );
        System.out.println( "Creator=" + info.getCreator() );
        System.out.println( "Producer=" + info.getProducer() );
        System.out.println( "Creation Date=" +  info.getCreationDate() );
        System.out.println( "Modification Date=" +  info.getModificationDate() );
        System.out.println( "Trapped=" + info.getTrapped() );
        if( metadata != null )
        {
            String string =  new String( metadata.toByteArray(), "ISO-8859-1" );
            System.out.println( "Metadata=" + string );
        }
    }
}
