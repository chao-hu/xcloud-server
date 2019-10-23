package com.xxx.xcloud.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class HttpUtil {
    private static final String CODE_UTF8 = "UTF-8";
    private static final String POST_ERROR_MSG = "post请求提交失败:";
    private static final String GET_ERROR_MSG = "get请求提交失败:";
    private static final int CODE_SUCCESS = 200;

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private HttpUtil() {}

    /**
     * put请求
     *
     * @param url
     *            url地址
     * @param jsonParam
     *            参数
     * @param noNeedResponse
     *            不需要返回结果
     * @return
     */
    public static JSONObject httpPut(String url, JSONObject jsonParam) {
        // post请求返回结果
        JSONObject jsonResult = null;
        HttpPut method = new HttpPut(url);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            if (null != jsonParam) {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), CODE_UTF8);
                entity.setContentEncoding(CODE_UTF8);
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            CloseableHttpResponse response = httpClient.execute(method);
            jsonResult = getResponse(response, url);
        } catch (IOException e) {
            logger.error(POST_ERROR_MSG + url, e);
        }
        return jsonResult;
    }

    /**
     * 发送post请求
     *
     * @param url
     *            路径
     * @param jsonParam
     *            参数
     * @param noNeedResponse
     *            不需要返回结果
     * @return
     */
    public static JSONObject httpPost(String url, JSONObject jsonParam) {
        JSONObject jsonResult = null;
        // 创建httppost
        // 创建默认的httpClient实例.
        HttpPost httppost = new HttpPost(url);
        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
            if (null != jsonParam) {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), CODE_UTF8);
                entity.setContentEncoding(CODE_UTF8);
                entity.setContentType("application/json");
                httppost.setEntity(entity);
            }
            CloseableHttpResponse response = httpclient.execute(httppost);
            jsonResult = getResponse(response, url);
        } catch (ClientProtocolException | UnsupportedEncodingException e) {
            logger.error("错误 :" + e);
        } catch (IOException e) {
            logger.error("IOException 错误 :" + e);
        }
        return jsonResult;
    }

    /**
     * 发送post请求
     *
     * @param url
     *            路径
     * @param jsonParam
     *            参数
     * @param noNeedResponse
     *            不需要返回结果
     * @return
     */
    public static JSONObject httpPostString(String url, String jsonParam) {
        JSONObject jsonResult = null;
        // 创建httppost
        HttpPost httppost = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonParam, CODE_UTF8);
        httppost.setEntity(entity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建默认的httpClient实例.
            CloseableHttpResponse response = httpClient.execute(httppost);
            jsonResult = getResponse(response, url);
        } catch (IOException e) {
            logger.error("IOException 错误", e);
        }
        return jsonResult;
    }

    /**
     * 发送post请求
     *
     * @param url
     *            路径
     * @param params
     *            参数
     * @return
     */
    public static JSONObject httpPostString(String url, Map<String, String[]> params) {
        JSONObject jsonResult = null;
        // 创建httppost
        HttpPost httppost = new HttpPost(url);

        // 创建默认的httpClient实例.
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httppost.setEntity(new UrlEncodedFormEntity(map2Pairs(params), CODE_UTF8));
            CloseableHttpResponse response = httpClient.execute(httppost);
            jsonResult = getResponse(response, url);
        } catch (IOException e) {
            logger.error("", e);
        }
        return jsonResult;
    }

    /**
     * 发送post请求
     *
     * @param url
     *            路径
     * @return
     */
    public static JSONObject httpPost(String url) {
        JSONObject jsonResult = null;
        // 创建httppost
        // 创建默认的httpClient实例.
        HttpPost httppost = new HttpPost(url);
        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
            CloseableHttpResponse response = httpclient.execute(httppost);
            jsonResult = getResponse(response, url);
        } catch (ClientProtocolException | UnsupportedEncodingException e) {
            logger.error("错误信息 :", e);
        } catch (IOException e) {
            logger.error("IOException 错误信息 :", e);
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url
     *
     * @return JSONObject
     */
    public static JSONObject httpGet(String url) {
        // get请求返回结果
        JSONObject jsonResult = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            jsonResult = getResponse(response, url);
        } catch (ClientProtocolException | ParseException e) {
            logger.error("错误信息:", e);
        } catch (IOException e) {
            logger.error("IOException 错误信息:", e);
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url
     *
     * @return JSONArray
     */
    public static JSONArray httpGetArray(String url) {
        // get请求返回结果
        JSONArray jsonArray = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            jsonArray = getResponseArray(response, url);
        } catch (ClientProtocolException | ParseException e) {
            logger.error("错误信息：", e);
        } catch (IOException e) {
            logger.error("IOException 错误信息：", e);
        }
        return jsonArray;
    }

    public static JSONObject httpDelete(String url) {
        // get请求返回结果
        JSONObject jsonResult = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // 发送get请求
            HttpDelete request = new HttpDelete(url);
            // 执行get请求.
            CloseableHttpResponse response = client.execute(request);
            jsonResult = getResponse(response, url);
        } catch (IOException e) {
            logger.error(GET_ERROR_MSG + url, e);
        }
        return jsonResult;
    }

    /**
     * 参数集合转换成 NameValuePair
     *
     * @param params
     *            参数集合
     * @return NameValuePair集合
     */
    private static List<NameValuePair> map2Pairs(Map<String, String[]> params) {
        List<NameValuePair> listPairs = new ArrayList<>();

        if (null != params) {
            Iterator<Map.Entry<String, String[]>> iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String[]> entry = iter.next();
                listPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()[0]));
            }
        }

        return listPairs;
    }

    public static JSONObject getResponse(CloseableHttpResponse response, String url) {
        JSONObject jsonResult = null;
        String urls = null;
        try {
            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /** 读取服务器返回过来的json字符串数据 **/
                String strResult = EntityUtils.toString(response.getEntity());
                /** 把json字符串转换成json对象 **/
                jsonResult = JSONObject.parseObject(strResult);
                urls = URLDecoder.decode(url, CODE_UTF8);
            } else {
                logger.error(GET_ERROR_MSG + urls);
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        finally {
            try {
                response.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        return jsonResult;
    }

    public static JSONArray getResponseArray(CloseableHttpResponse response, String url) {
        JSONArray jsonResult = null;
        try {
            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /** 读取服务器返回过来的json字符串数据 **/
                String strResult = EntityUtils.toString(response.getEntity());
                /** 把json字符串转换成json对象 **/
                jsonResult = JSONObject.parseArray(strResult);
            } else {
                logger.error(GET_ERROR_MSG + url);
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return jsonResult;
    }

    /**
     * 文件下载
     *
     * @param servletResponse
     */
    public static void httpDownload(HttpServletResponse servletResponse, String url, String logName) {

        String strResult = httpDownloadZip(url);

        try {
            HttpUtil.fileDownLoad(servletResponse, strResult, logName);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static boolean fileDownLoad(HttpServletResponse response, String str, String logName) {
        boolean ret = false;
        if (null != response) {

            // 提示框设置
            response.reset(); // reset the response
            response.setContentType("application/octet-stream");// 告诉浏览器输出内容为流
            response.setHeader("content-disposition", "attachment; filename=\"" + logName + "\"");

            try {

                InputStream inputStream = new ByteArrayInputStream(str.getBytes());

                BufferedInputStream buff = new BufferedInputStream(inputStream);
                byte[] aryByte = new byte[1024];// 缓存
                long k = 0;// 该值用于计算当前实际下载了多少字节
                // 输出流
                OutputStream out = response.getOutputStream();
                // 开始循环下载
                while (k < str.length()) {
                    int j = buff.read(aryByte, 0, 1024);
                    k += j;
                    // 将b中的数据写到客户端的内存
                    out.write(aryByte, 0, j);
                }

                // 关闭输出流
                out.flush();
                out.close();
                inputStream.close();
                buff.close();

                ret = true;
                logger.info("文件下载完毕！");
            } catch (Exception e) {

                logger.error("下载失败！", e);
            }

        } else {

            throw new NullPointerException("HttpServletRequest Or HttpServletResponse Or fileName Is Null !");
        }
        return ret;
    }

    /**
     * zip文件下载
     *
     * @param servletResponse
     */
    public static String httpDownloadZip(String url) {
        String strResult = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /** 读取服务器返回过来的json字符串数据 **/
                strResult = EntityUtils.toString(response.getEntity());
            } else {
                logger.error(GET_ERROR_MSG + url);
            }
        } catch (IOException e) {
            logger.error(GET_ERROR_MSG + url, e);
        }

        return strResult;
    }

    public static boolean fileDownLoadZip(HttpServletResponse response, Map<String, String> str, String logName) {

        String folder = System.getProperty("java.io.tmpdir");
        File zipFile = new File(folder + "/" + logName);

        // 提示框设置
        response.reset(); // reset the response
        response.setContentType("application/octet-stream");// 告诉浏览器输出内容为流
        response.setHeader("content-disposition", "attachment; filename=\"" + logName + "\"");

        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile))) {

            writeToOut(str, zout);

            zout.close();

            // 从zipfile输出到浏览器
            InputStream input = new FileInputStream(zipFile);

            OutputStream out = response.getOutputStream();
            int temp = 0;
            while ((temp = input.read()) != -1) {
                out.write(temp);
            }
            out.flush();
            out.close();
            input.close();

            // 删除临时文件
            zipFile.delete();

        } catch (Exception e) {

            logger.error("文件找不到！", e);
            return false;
        }

        return false;
    }

    private static void writeToOut(Map<String, String> str, ZipOutputStream zout) throws IOException {

        for (Entry<String, String> entity : str.entrySet()) {
            InputStream inputStream;
            BufferedInputStream buff;
            inputStream = new ByteArrayInputStream(entity.getValue().getBytes());
            buff = new BufferedInputStream(inputStream);
            zout.putNextEntry(new ZipEntry(entity.getKey())); // 获取文件名称
            byte[] aryByte = new byte[1024];// 缓存
            long k = 0;// 该值用于计算当前实际下载了多少字节
            while (k < entity.getValue().length()) {
                int j = buff.read(aryByte, 0, 1024);
                k += j;
                // 将b中的数据写到客户端的内存
                zout.write(aryByte, 0, j);
            }
            zout.flush();

            inputStream.close();
            buff.close();
        }

    }

    /**
     * doGetStream:通过get获取网络资源. <br/>
     *
     * @author longkaixiang
     * @param url
     * @param params
     * @return InputStream
     */
    public static InputStream doGetStream(String url, Map<String, Object> params) {
        logger.info("调用：-" + url + "接口，参数列表：-" + params);
        String parameterData = null;
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        try {
            if (params != null) {
                parameterData = "";
                for (String key : params.keySet()) {
                    parameterData += ("".equals(parameterData) ? "" : "&") + key + "="
                            + URLEncoder.encode(String.valueOf(params.get(key)).replace(" ", ""), "UTF8");
                }
            }

            // 获得一个http连接
            HttpURLConnection httpURLConnection = getHttpURLConnForJson(url, parameterData, "GET");

            if (httpURLConnection.getResponseCode() == CODE_SUCCESS) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // 关闭输入输出流
            closeStream(outputStream, outputStreamWriter, null, null, null);
        }
        return inputStream;
    }

    private static HttpURLConnection getHttpURLConnForJson(String url, String parameterData, String mode)
            throws MalformedURLException, IOException, ProtocolException {
        URL localURL;
        if (parameterData != null) {
            localURL = new URL(url + "?" + parameterData);
        } else {
            localURL = new URL(url);
        }
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        // 设置连接超时为10s
        httpURLConnection.setConnectTimeout(10000);
        // 读取数据超时也是10s
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.setRequestMethod(mode);
        return httpURLConnection;
    }

    /**
     * 关闭输入输出流
     *
     * @param outputStream
     *            OutputStream
     * @param outputStreamWriter
     *            OutputStreamWriter
     * @param inputStream
     *            InputStream
     * @param inputStreamReader
     *            InputStreamReader
     * @param reader
     *            BufferedReader
     * @see IOException
     * @exception IOException
     */
    private static void closeStream(OutputStream outputStream, OutputStreamWriter outputStreamWriter,
            InputStream inputStream, InputStreamReader inputStreamReader, BufferedReader reader) {
        if (outputStreamWriter != null) {
            try {
                outputStreamWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStreamReader != null) {
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
