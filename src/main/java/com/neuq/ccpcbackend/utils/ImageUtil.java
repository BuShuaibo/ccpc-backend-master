package com.neuq.ccpcbackend.utils;

import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author MachiskComic
 * @ClassName ImageToZipUtil
 * @date 2025-04-17 19:09
 */
public class ImageUtil {
    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    /**
     * 将多张网络图片打包为ZIP下载
     * @param sourceFileName 下载文件名（不含扩展名）
     * @param urlList 图片URL列表
     * @param response HTTP响应对象
     */
    public static void imagesToZip(String sourceFileName, List<String> urlList, List<String> imageNames, HttpServletResponse response) {
        // 参数校验
        if (urlList == null || urlList.isEmpty()) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "图片列表不能为空");
        }

        try {
            // 设置响应头
            setZipResponseHeaders(response, sourceFileName);

            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
                for (int i = 0; i < urlList.size(); i++) {
                    String imageUrl = urlList.get(i);
                    processSingleImage(zipOut, imageUrl, imageNames.get(i), i + 1);
                }
                zipOut.finish();
            }
        } catch (Exception e) {
            log.error("图片打包失败 | 文件名: {} | 错误: {}", sourceFileName, e.getMessage());
            throw new BizException(ErrorCode.DOWNLOAD_FAILED.getErrCode(), "文件打包失败");
        }
    }

    /**
     * 处理单张图片下载并加入ZIP
     */
    private static void processSingleImage(ZipOutputStream zipOut, String imageUrl, String imageName, int index) {
        HttpURLConnection connection = null;
        try {
            // 创建连接并处理重定向
            connection = createConnection(imageUrl);
            validateResponse(connection);

            // 获取文件名和扩展名
            String contentType = connection.getContentType();
            String fileExtension = getFileExtension(contentType, connection.getURL().getPath());
            validateImageFormat(fileExtension);

            // 写入ZIP条目
            try (InputStream input = connection.getInputStream()) {
                addToZip(zipOut, imageName + ".png", input);
            }
        } catch (Exception e) {
            log.warn("图片下载失败 | URL: {} | 原因: {}", imageUrl, e.getMessage());
            throw new BizException(ErrorCode.DOWNLOAD_FAILED.getErrCode(), "第"+index+"张图片下载失败");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 创建HTTP连接并处理重定向
     */
    private static HttpURLConnection createConnection(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setInstanceFollowRedirects(true);

        // 处理重定向
        int status = connection.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_MOVED_TEMP) {
            String newUrl = connection.getHeaderField("Location");
            return createConnection(newUrl);
        }
        return connection;
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String contentType, String path) {
        // 优先根据Content-Type判断
        if (contentType != null) {
            if (contentType.contains("png")) return "png";
            if (contentType.contains("jpeg") || contentType.contains("jpg")) return "jpg";
        }

        // 从路径解析（处理含查询参数的URL）
        String cleanPath = path.split("[?#]")[0]; // 移除查询参数和锚点
        int lastDot = cleanPath.lastIndexOf('.');
        if (lastDot != -1 && lastDot < cleanPath.length() - 1) {
            String ext = cleanPath.substring(lastDot + 1).toLowerCase();
            if (ext.equals("jpeg")) return "jpg";
            if (ext.equals("jpg") || ext.equals("png")) return ext;
        }
        return "jpg"; // 默认值
    }

    /**
     * 校验图片格式合法性
     */
    private static void validateImageFormat(String extension) {
        if (!extension.equalsIgnoreCase("jpg")
                && !extension.equalsIgnoreCase("png")) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(),
                    "不支持的图片格式: " + extension);
        }
    }

    /**
     * 设置ZIP响应头
     */
    private static void setZipResponseHeaders(HttpServletResponse response, String baseName)
            throws UnsupportedEncodingException {
        String encodedFilename = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType("application/octet-stream;character=utf-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFilename + ".zip\";" +
                        "filename*=UTF-8''" + encodedFilename + ".zip");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    /**
     * 将数据流添加到ZIP
     */
    private static void addToZip(ZipOutputStream zipOut, String entryName, InputStream input)
            throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zipOut.putNextEntry(entry);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            zipOut.write(buffer, 0, bytesRead);
        }
        zipOut.closeEntry();
    }

    /**
     * 验证HTTP响应状态
     */
    private static void validateResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new BizException(ErrorCode.DOWNLOAD_FAILED.getErrCode(),
                    "图片服务器响应异常: HTTP " + status);
        }

        // 校验内容类型
        String contentType = connection.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/")
                        && !contentType.equals("application/octet-stream"))) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(),
                    "非图片内容类型: " + contentType);
        }
    }
}