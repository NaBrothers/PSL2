package com.nabrothers.psl.server.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private static int line_height = 18;
    /**
     * 字体
     */
    private static Font font;

    private static final String DEFAULT_PATH = "./go-cqhttp/data/images/cache/";

    private static final String FONT_PATH = "./assets/sarasa.ttf";

    private static final int FONT_SIZE = 14;

    private static final int LINE_CHAR_COUNT = 50*2; //每行最大字符数

    @PostConstruct
    public void init() {
        Path path = Paths.get(DEFAULT_PATH);
        try {
            if (!path.toFile().exists()) {
                Files.createDirectories(path);
            } else {
                File files[] = path.toFile().listFiles();
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            }
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

    private static boolean isChinese(char ch) {
        return String.valueOf(ch).getBytes(StandardCharsets.UTF_8).length == 3;
    }

    public static String toImage(String message) {
        String fileName = String.format("%s.jpg",  message.hashCode());
        File file = new File(DEFAULT_PATH + fileName);
        if (file.exists()) {
            return "cache/" + fileName;
        }

        String[] strArr = message.split("\n");
        FontMetrics fm = FontDesignMetrics.getMetrics(font);
        int stringWidth = fm.charWidth('a');// 标点符号也算一个字

        // 自适应画布
        int maxCount = -1;
        for (String str : strArr) {
            int strCount = 0;
            for (char ch : str.toCharArray()) {
                if (isChinese(ch)) {
                    strCount += 2;
                } else {
                    strCount += 1;
                }
            }
            if (strCount  > maxCount) {
                maxCount = strCount;
            }
        }

        int maxLineCount = Math.min(maxCount, LINE_CHAR_COUNT);
        int width = maxLineCount * stringWidth;

        //将数组转为list
        List<String> strList = new ArrayList<>(Arrays.asList(strArr));

        //按照每行多少个字符进行分割
        for (int j = 0; j < strList.size(); j++) {
            String str = strList.get(j);
            int strCount = 0;
            int index = 0;
            for (char ch : str.toCharArray()) {
                if (isChinese(ch)) {
                    strCount += 2;
                } else {
                    strCount += 1;
                }
                //当字符数超过限制，就进行分割
                if (strCount > maxLineCount) {
                    //将多的那一端放入本行下一行，等待下一个循环处理
                    strList.add(j + 1, str.substring(index));
                    //更新本行的内容
                    strList.set(j, str.substring(0, index));
                    break;
                }
                index++;
            }
        }

        //计算图片的高度，多预留半行
        int image_height = strList.size() * line_height + line_height/2;

        //每张图片有多少行文字
        int every_line = image_height / line_height;


        for (int m = 0; m < 1; m++) {
            File outFile = new File(DEFAULT_PATH + fileName);
            // 创建图片，宽度多预留一点
            BufferedImage image = new BufferedImage(width + FONT_SIZE, image_height,
                    BufferedImage.TYPE_INT_BGR);
            Graphics g = image.getGraphics();
            g.setClip(0, 0, width + FONT_SIZE, image_height);
            g.setColor(Color.white); // 背景色白色
            g.fillRect(0, 0, width + FONT_SIZE, image_height);

            g.setColor(Color.black);//  字体颜色黑色
            g.setFont(font);// 设置画笔字体

            // 每张多少行，当到最后一张时判断是否填充满
            for (int i = 0; i < every_line; i++) {
                int index = i + m * every_line;
                if (strList.size() - 1 >= index) {
//                    System.out.println("每行实际=" + newList.get(index).length());
                    g.drawString(strList.get(index), FONT_SIZE / 2, line_height * (i + 1));
                }
            }
            g.dispose();
            try {
                ImageIO.write(image, "jpg", outFile);// 输出png图片
            } catch (Exception e) {
                log.error(e);
            }
        }
        return "cache/" + fileName;
    }

}