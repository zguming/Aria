/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.download.downloader;

import android.text.TextUtils;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.ProtocolType;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.util.SSLContextUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by lyy on 2017/1/18.
 * 链接帮助类
 */
class ConnectionHelp {

  /**
   * 转换HttpUrlConnect的inputStream流
   *
   * @return {@link GZIPInputStream}、{@link InflaterInputStream}
   * @throws IOException
   */
  static InputStream convertInputStream(HttpURLConnection connection) throws IOException {
    String encoding = connection.getHeaderField("Content-Encoding");
    if (TextUtils.isEmpty(encoding)) {
      return connection.getInputStream();
    }
    if (encoding.contains("gzip")) {
      return new GZIPInputStream(connection.getInputStream());
    } else if (encoding.contains("deflate")) {
      return new InflaterInputStream(connection.getInputStream());
    } else {
      return connection.getInputStream();
    }
  }

  /**
   * 处理链接
   *
   * @throws IOException
   */
  static HttpURLConnection handleConnection(URL url, AbsTaskEntity taskEntity) throws IOException {
    HttpURLConnection conn;
    URLConnection urlConn;
    if (taskEntity.getProxy() != null) {
      urlConn = url.openConnection(taskEntity.getProxy());
    } else {
      urlConn = url.openConnection();
    }
    if (urlConn instanceof HttpsURLConnection) {
      AriaManager manager = AriaManager.getInstance(AriaManager.APP);
      conn = (HttpsURLConnection) urlConn;
      SSLContext sslContext =
          SSLContextUtil.getSSLContextFromAssets(manager.getDownloadConfig().getCaName(),
              manager.getDownloadConfig().getCaPath(), ProtocolType.Default);
      if (sslContext == null) {
        sslContext = SSLContextUtil.getDefaultSLLContext(ProtocolType.Default);
      }
      SSLSocketFactory ssf = sslContext.getSocketFactory();
      ((HttpsURLConnection) conn).setSSLSocketFactory(ssf);
      ((HttpsURLConnection) conn).setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
    } else {
      conn = (HttpURLConnection) urlConn;
    }
    return conn;
  }

  /**
   * 设置头部参数
   *
   * @throws ProtocolException
   */
  static HttpURLConnection setConnectParam(DownloadTaskEntity entity, HttpURLConnection conn) {
    if (entity.getRequestEnum() == RequestEnum.POST) {
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
    }
    Set<String> keys = null;
    if (entity.getHeaders() != null && entity.getHeaders().size() > 0) {
      keys = entity.getHeaders().keySet();
      for (String key : keys) {
        conn.setRequestProperty(key, entity.getHeaders().get(key));
      }
    }
    if (conn.getRequestProperty("Accept-Language") == null) {
      conn.setRequestProperty("Accept-Language", "UTF-8");
    }
    if (conn.getRequestProperty("Accept-Encoding") == null) {
      conn.setRequestProperty("Accept-Encoding", "identity");
    }
    if (conn.getRequestProperty("Accept-Charset") == null) {
      conn.setRequestProperty("Accept-Charset", "UTF-8");
    }
    if (conn.getRequestProperty("Connection") == null) {
      conn.setRequestProperty("Connection", "Keep-Alive");
    }
    if (conn.getRequestProperty("Charset") == null) {
      conn.setRequestProperty("Charset", "UTF-8");
    }
    if (conn.getRequestProperty("User-Agent") == null) {
      conn.setRequestProperty("User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
    }
    if (conn.getRequestProperty("Accept") == null) {
      //StringBuilder accept = new StringBuilder();
      //accept
      //.append("image/gif, ")
      //.append("image/jpeg, ")
      //.append("image/pjpeg, ")
      //.append("image/webp, ")
      //.append("image/apng, ")
      //.append("application/xml, ")
      //.append("application/xaml+xml, ")
      //.append("application/xhtml+xml, ")
      //.append("application/x-shockwave-flash, ")
      //.append("application/x-ms-xbap, ")
      //.append("application/x-ms-application, ")
      //.append("application/msword, ")
      //.append("application/vnd.ms-excel, ")
      //.append("application/vnd.ms-xpsdocument, ")
      //.append("application/vnd.ms-powerpoint, ")
      //.append("text/plain, ")
      //.append("text/html, ")
      //.append("*/*");
      conn.setRequestProperty("Accept", "*/*");
    }
    //302获取重定向地址
    conn.setInstanceFollowRedirects(false);
    return conn;
  }
}
