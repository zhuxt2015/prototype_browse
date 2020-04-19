package com.zhuxt.prototype;

import spark.utils.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 * @author zhuxuetong
 * @date 2020/4/14 18:58
 */
public class Application {
    private static String path = "";
    private static String configFile = "prototype.properties";
    private static String viewType = "交互图";

    public static void main(String[] args) throws IOException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(configFile);
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
        //模块列表
        get("/prototype/:product", (request, response) -> {
            String productName = request.params("product");
            StringBuilder url = new StringBuilder(productName).append("/").append(viewType);
            List<String> moduleNames = getDirectories(finalPath, url.toString());
            StringBuilder htmlBuffer = getHtml(url.toString(), moduleNames);
            return htmlBuffer.toString();
        });
        //图片列表or设计稿
        get("/prototype/:product/:viewType/:module", (request, response) -> {
            String productName = request.params("product");
            String moduleName = request.params("module");
            String viewType = request.params("viewType");
            List<String> fileNames = getPictures(finalPath, productName, viewType, moduleName);
            StringBuilder htmlBuffer = getHtml(moduleName, fileNames);
            return htmlBuffer.toString();
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
            try {
                byte[] b = new byte[1024];
                int length = -1;
                while ((length=fileInputStream.read(b)) != -1) {
                    outputStream.write(b, 0, length);
                }
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                outputStream.close();
                fileInputStream.close();
            }
            return outputStream.toByteArray();
        });


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
        htmlBuffer.append("<style>\n" + "\t\ta {\n" + "\t\t\tcolor: #525C66;\n" + "\t\t\ttext-decoration: none;\n" + "\t\t}\n" + "        <!-- div 大小设定 -->\n" + "\t\t.top-10 {\n" + "\t\t\tfloat: left;\n" + "\t\t\twidth: 340px;\n" + "\n" + "\t\t\tmargin-top: 106px;\n" + "\t\t\tmargin-left: 118px;\n" + "\t\t\tbackground: #fff;\n" + "\t\t\tborder: 1px solid #FFF;\n" + "\t\t\tbox-shadow: #d0d0d0 1px 1px 10px 0px;\n" + "\t\t}\n" + "\n" + "\t\t.top-10 ul {\n" + "\t\t\tcounter-reset: section;\n" + "\t\t}\n" + "\n" + "\t\t.top-10 li {\n" + "\t\t\t\n" + "\t\t\twidth: 260px;\n" + "\t\t\tborder-bottom: 1px solid #b8c2cc;\n" + "\t\t\tline-height: 46px;\n" + "\t\t\theight: 46px;\n" + "\t\t\toverflow: hidden;\n" + "\t\t\tcolor: #525C66;\n" + "\t\t\tfont-size: 14px;\n" + "\n" + "\t\t}\n" + "\n" + "\t\t.top-10 li:before {\n" + "\t\t\tcounter-increment: section;\n" + "\t\t\tcontent: counter(section);\n" + "\t\t\tdisplay: inline-block;\n" + "\t\t\tpadding: 0 12px;\n" + "\t\t\tmargin-right: 10px;\n" + "\t\t\theight: 18px;\n" + "\t\t\tline-height: 18px;\n" + "\t\t\tbackground: #0164b4;\n" + "\t\t\tcolor: #fff;\n" + "\t\t\tborder-radius: 3px;\n" + "\t\t\tfont-size: 9px\n" + "\t\t}\n" + "        <!-- 排名前三名颜色控制 -->\n" + "\t\t.top-10 li:nth-child(1):before {\n" + "\t\t\tbackground: #0164b4\n" + "\t\t}\n" + "\n" + "\t\t.top-10 li:nth-child(2):before {\n" + "\t\t\tbackground: #0164b4\n" + "\t\t}\n" + "\n" + "\t\t.top-10 li:nth-child(3):before {\n" + "\t\t\tbackground: #0164b4\n" + "\t\t}\n" + "\t</style>\n");
        htmlBuffer.append("<body>").append("\n");
        htmlBuffer.append("<div class=\"top-10\">").append("\n");
        htmlBuffer.append("<ul>").append("\n");

        for (String productName : directoryNames) {
            htmlBuffer.append("<li><a href=\"").append(prefix).append("/").append(productName).append("\" target=\"_blank\">").append(productName).append("</a></li>").append("\n");
        }
        htmlBuffer.append("</ul>").append("\n");
        htmlBuffer.append("</div>").append("\n");
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
