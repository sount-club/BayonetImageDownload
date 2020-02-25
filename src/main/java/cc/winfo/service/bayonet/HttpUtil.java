package cc.winfo.service.bayonet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final OkHttpClient CLIENT;

    private static final long connectTimeOut = 10000;
    private static final long readTimeOut = 10000;
    private static final long writeTimeOut = 10000;

    static {
        //连接池
        ConnectionPool connectionPool = new ConnectionPool(10,60,TimeUnit.SECONDS);

        CLIENT= new OkHttpClient.Builder()
                .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .build();
    }


    public static Call get(String url) {

        Request request = new Request.Builder().get().url(url).build();
        return  CLIENT.newCall(request);
    }


    public static  String string(Call call) throws IOException {
        Response response = call.execute();

        return response.body().string();


    }


    public static  <T> T json(Call call,Class<T> clazz) throws IOException {

        String resp = string(call);
        return JSON.parseObject(resp, clazz);

    }

    public static JSONObject json(Call call) throws IOException {

        String resp = string(call);
        return JSON.parseObject(resp);

    }


    public static void dowload(Call call, final String path,final String destFileName){

        call.enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                logger.error("下载失败");
            }

            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);

                        logger.debug("[{}]已下载：{}",destFileName,progress);
                    }
                    logger.info("[{}]已下载完成",destFileName);
                    fos.flush();
                } catch (Exception e) {
                } finally {

                    try {
                        if (is != null){
                            is.close();
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null){

                            fos.close();
                        }
                    } catch (IOException e) {
                    }


                }
            }

        });
    }

    public static void main(String[] args) throws IOException {

        String url = "https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1582553579&di=06898ae87e48ba5fc5bb0a6ce71d1445&src=http://a3.att.hudong.com/68/61/300000839764127060614318218_950.jpg";
//        String url = "http://www.baidu.com" ;
        Call call = HttpUtil.get(url);

//        String string = string(call);

//        System.out.println(string);
        dowload(call, "D://", "baidu.jpg");
    }

}
