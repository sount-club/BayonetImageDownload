package cc.winfo.service.bayonet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ImageDownload {

    static Logger logger = LoggerFactory.getLogger(ImageDownload.class);
    static String url = "http://198.18.127.220:80/crosssection/getShipDetailsList.action?pageSize=1000000&currentPage=1&hhuser=gz_dwjk&hhpwd=gz_dwjk";

    public static void main(String[] args) throws IOException {


        Call call = HttpUtil.get(url);
        JSONObject imgDataJson = HttpUtil.json(call, JSONObject.class);
        if ("1".equals(imgDataJson.getString("result"))){
            JSONObject data1 = imgDataJson.getJSONObject("data");
            JSONArray jsonArray1 = data1.getJSONArray("list");
            logger.info("数量 : [{}]",jsonArray1.size());
            if (jsonArray1.size() > 0) {
                for (int a = 0; a < jsonArray1.size(); a++) {
                    JSONObject jsonObject2 = (JSONObject) jsonArray1.get(a);
                    //Set<String> set = ()jsonObject2.getObject(jsonObject2.getString("imags"), String.class);
                    JSONArray jsonArray2 = jsonObject2.getJSONArray("imags");
                    String shipName = jsonObject2.getString("shipName");
                    String mmsi = jsonObject2.getString("mmsi");
                    if(StringUtils.isNoneBlank(shipName)){
                        shipName = "DG";
                    }
                    if(StringUtils.isAllBlank(mmsi)){
                        mmsi = "000000";
                    }
                    if (jsonArray2.size() > 0) {
                        //jsonArray2.
                        for (int b = 0; b < jsonArray2.size(); b++) {
                            JSONObject jj = (JSONObject) jsonArray2.get(b);
                            String image = jj.getString("pathpic");
                            if (StringUtils.isNotBlank(image)) {
                                System.out.println("image : "+image);
                                download(image,shipName,mmsi);
                            }
                        }
                    }
                    //count += jsonArray2.size();
                }
            }
        }

    }


    //图片下载
    private static void download(String img, String shipName, String mmsi){
        //imgPath =
        String imgPath = img;
        String urlPath = imgPath.replace("\\", "/");
        //urlPath.replace("\\", "\\");
        urlPath = urlPath.replace("E:", "http://198.18.127.206");
        try {
            // 构造URL
            logger.info("url : {}",urlPath);

            /*此处也可用BufferedInputStream与BufferedOutputStream*/
            /* 构建文件目录 */
            //imgPath.sub
            String path = "F://dgImg";
            String[] imgArr =  imgPath.split("\\\\");
            String dataPath =  imgArr[2];
            path = path +"/"+dataPath.substring(0,4);
            path = path+"/"+dataPath.substring(4,6);
            path = path+"/"+dataPath.substring(6,8);
            //创建文件对象
            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            logger.info("图片保存路径 : [{}]",path);
            String fileName = "/"+shipName+"-"+mmsi+"-"+dataPath+"-"+System.currentTimeMillis()+".jpg";

            Call call = HttpUtil.get(urlPath);
            HttpUtil.dowload(call,path,fileName);

        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
    }
}
