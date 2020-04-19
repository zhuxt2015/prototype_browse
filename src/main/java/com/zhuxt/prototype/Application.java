package com.zhuxt.prototype;

import spark.utils.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static spark.Spark.*;

/**
 * @author zhuxuetong
 * @date 2020/4/14 18:58
 */
public class Application {
    private static String UE = "UE交互图";
    private static String UI = "UI设计稿";
    private static String path = "";
    public static void main(String[] args) throws IOException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("prototype.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        int port = 4567;
        String portValue = properties.getProperty("prototype.port");
        if (StringUtils.isNotEmpty(portValue)) {
            port = Integer.parseInt(portValue);
        }
        path = properties.getProperty("prototype.path");
        if (StringUtils.isEmpty(path)) {
            throw new RuntimeException("请配置prototype.path");
        }
        port(port);
        String finalPath = path;
        //产品列表
        get("/prototype", (request, response) -> {
            List<String> productNames = getDirectories(finalPath);
            StringBuilder htmlBuffer = getHtml("prototype", productNames);
            return htmlBuffer.toString();
        });
        //UE交互图或者UI设计稿
        get("/prototype/:product", (request, response) -> {
            String product = request.params("product");
            List<String> productNames = new ArrayList<>();
            productNames.add(UE);
            productNames.add(UI);
            StringBuilder htmlBuffer = getHtml(product, productNames);
            return htmlBuffer.toString();
        });
        //模块列表
        get("/prototype/:product/:viewType", (request, response) -> {
            String productName = request.params("product");
            String viewType = request.params("viewType");
            StringBuilder url = new StringBuilder(productName).append("/");
            String prefixPath = "";
            if (viewType.equals(UE)) {
                prefixPath = "交互图";
                url.append(prefixPath);
            } else if (viewType.equals(UI)) {
                prefixPath = "设计图" + File.separator + "设计稿html";
                url.append(prefixPath);
            } else {
                return "url路径不正确";
            }
            List<String> moduleNames = getDirectories(finalPath, url.toString());
            StringBuilder htmlBuffer = getHtml(prefixPath, moduleNames);
            return htmlBuffer.toString();
        });
        //图片列表or设计稿
        get("/prototype/:product/:viewType/:module", (request, response) -> {
            String productName = request.params("product");
            String viewType = request.params("viewType");
            String moduleName = request.params("module");
            if (viewType.equals(UE)) {
                viewType = "交互图";
                List<String> fileNames = getPictures(finalPath, productName, viewType, moduleName);
                StringBuilder htmlBuffer = getHtml(moduleName, fileNames);
                return htmlBuffer.toString();
            } else if (viewType.equals(UI)) {
                viewType = "设计图" + File.separator + "设计稿html";
                return getUIHtml(finalPath, productName, viewType, moduleName);
            }else {
                return "url路径不正确";
            }
        });
        //图片展示
        get("/prototype/:product/:viewType/:module/:picture", (request, response) -> {
            response.type("image/png");
            String productName = request.params("product");
            String designName = request.params("viewType");
            String moduleName = request.params("module");
            String pictureName = request.params("picture");
            File picture = getPicture(finalPath, productName + File.separator + designName + File.separator + moduleName + File.separator + pictureName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FileInputStream fileInputStream = new FileInputStream(picture);
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = fileInputStream.read(b)) != -1) {
                outputStream.write(b, 0, b.length);
            }
            outputStream.flush();
            outputStream.close();
            fileInputStream.close();
            return outputStream.toByteArray();
        });


    }

    private static String getUIHtml(String finalPath, String productName, String viewType, String moduleName) {
        File file = new File(finalPath + File.separator + productName + File.separator + viewType + File.separator + moduleName);
        File[] files = file.listFiles();
        if (null == files) {
            return null;
        }
        String content = "";
        for (File productFile : files) {
            String name = productFile.getName();
            if (productFile.isFile() && name.equals("index.html")) {
                try {
                    content = new String(Files.readAllBytes(productFile.toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return content;
    }

    private static File getPicture(String finalPath, String picturePath) {
        return new File(finalPath + File.separator + picturePath);
    }

    private static List<String> getPictures(String finalPath, String productName, String designName, String moduleName) {
        File file = new File(finalPath + File.separator + productName + File.separator + designName + File.separator + moduleName);
        File[] files = file.listFiles();
        if (null == files) {
            return new ArrayList<>();
        }
        List<String> paths = new ArrayList<>();
        for (File productFile : files) {
            String name = productFile.getName();
            if (productFile.isFile() && !name.startsWith(".")) {
                paths.add(name);
            }
        }
        return paths;
    }

    private static List<String> getDirectories(String finalPath, String productName) {
        return getDirectories(finalPath + File.separator + productName);
    }

    private static StringBuilder getHtml(String prefix, List<String> directoryNames) {
        StringBuilder htmlBuffer = new StringBuilder();
        htmlBuffer.append("<html>").append("\n");
        htmlBuffer.append("<body>").append("\n");
        htmlBuffer.append("<ul>").append("\n");

        for (String productName : directoryNames) {
            htmlBuffer.append("<li><a href=\"").append(prefix).append("/").append(productName).append("\" target=\"_blank\">").append(productName).append("</a></li>").append("\n");
        }
        htmlBuffer.append("</ul>").append("\n");
        htmlBuffer.append("</body>").append("\n");
        htmlBuffer.append("</html>").append("\n");
        return htmlBuffer;
    }

    private static List<String> getDirectories(String finalPath) {
        File file = new File(finalPath);
        File[] files = file.listFiles();
        if (null == files) {
            return new ArrayList<>();
        }
        List<String> paths = new ArrayList<>();
        for (File productFile : files) {
            String name = productFile.getName();
            if (productFile.isDirectory() && !name.startsWith(".")) {
                paths.add(name);
            }
        }
        return paths;
    }
}
