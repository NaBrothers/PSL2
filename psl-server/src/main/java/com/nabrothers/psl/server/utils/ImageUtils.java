package com.nabrothers.psl.server.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

/**
 * 如果一行文字长度超过了既定的宽度，那么主动执行换行操作
 */
@Log4j2
@Component
public class ImageUtils {

    /**
     * 图片宽度
     */
    private static int width = 400;
    /**
     * 每一行的高度
     */
    private static int line_height = 18;
    /**
     * 字体
     */
    private static Font font;

    private static final String DEFAULT_PATH = "./go-cqhttp/data/images/text2image/";

    private static final String FONT_PATH = "./assets/sarasa.ttf";

    private static final int FONT_SIZE = 14;

    @PostConstruct
    public void init() {
        Path path = Paths.get(DEFAULT_PATH);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error(e);
        }

        log.info("加载图片解析器");
        font = loadFont();
        toImage(String.valueOf(System.currentTimeMillis()));
        log.info("图片解析器加载完毕");
    }

    private static Font loadFont() {
        try {
            File file = new File(FONT_PATH);
            FileInputStream aixing = new FileInputStream(file);
            Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, aixing);
            Font dynamicFontPt = dynamicFont.deriveFont(Font.PLAIN, FONT_SIZE);
            aixing.close();
            return dynamicFontPt;
        } catch(Exception e) {
            log.error(e);
            log.warn("加载自定义字体失败，使用默认字体");
            return new Font("宋体", Font.PLAIN, FONT_SIZE);

        }
    }

    public static String toImage(String message) {
        String fileName = String.format("%s.jpg",  message.hashCode());
        File file = new File(DEFAULT_PATH + fileName);
        if (file.exists()) {
            return "text2image/" + fileName;
        }

        String[] strArr = message.split("\n");
        FontMetrics fm = FontDesignMetrics.getMetrics(font);
        int stringWidth = fm.charWidth('字');// 标点符号也算一个字
        //计算每行多少字 = 宽/每个字占用的宽度
        int line_string_num = width % stringWidth == 0 ? (width / stringWidth) : (width / stringWidth) + 1;

        //System.out.println("每行字数=" + line_string_num);
        //将数组转为list
        List<String> strList = new ArrayList<>(Arrays.asList(strArr));

        //按照每行多少个字进行分割
        for (int j = 0; j < strList.size(); j++) {
            //当字数超过限制，就进行分割
            if (strList.get(j).length() > line_string_num) {
                //将多的那一端放入本行下一行，等待下一个循环处理
                strList.add(j + 1, strList.get(j).substring(line_string_num));
                //更新本行的内容
                strList.set(j, strList.get(j).substring(0, line_string_num));
            }
        }

        //计算图片的高度，多预留半行
        int image_height = strList.size() * line_height + line_height/2;

        //每张图片有多少行文字
        int every_line = image_height / line_height;


        for (int m = 0; m < 1; m++) {
            File outFile = new File(DEFAULT_PATH + fileName);
            // 创建图片  宽度多预留一点
            BufferedImage image = new BufferedImage(width + 20, image_height,
                    BufferedImage.TYPE_INT_BGR);
            Graphics g = image.getGraphics();
            g.setClip(0, 0, width + 20, image_height);
            g.setColor(Color.white); // 背景色白色
            g.fillRect(0, 0, width + 20, image_height);

            g.setColor(Color.black);//  字体颜色黑色
            g.setFont(font);// 设置画笔字体

            // 每张多少行，当到最后一张时判断是否填充满
            for (int i = 0; i < every_line; i++) {
                int index = i + m * every_line;
                if (strList.size() - 1 >= index) {
//                    System.out.println("每行实际=" + newList.get(index).length());
                    g.drawString(strList.get(index), 0, line_height * (i + 1));
                }
            }
            g.dispose();
            try {
                ImageIO.write(image, "jpg", outFile);// 输出png图片
            } catch (Exception e) {
                log.error(e);
            }
        }
        return "text2image/" + fileName;
    }

}