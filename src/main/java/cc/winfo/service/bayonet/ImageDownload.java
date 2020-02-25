package cc.winfo.service.bayonet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageDownload {

    static Logger logger = LoggerFactory.getLogger(ImageDownload.class);
    static String url = "http://198.18.127.220:80/crosssection/getShipDetailsList.action?pageSize=1000000&currentPage=1&hhuser=gz_dwjk&hhpwd=gz_dwjk";

    static String path = "E:/bImgs";

    public static void main(String[] args) throws IOException {
        ImageDownload imageDownload = new ImageDownload();

        List<Long> bids = imageDownload.getBayonetIds();

        logger.info("卡口ID获取成功：【{}】",bids);

        for (int i = 0; i < bids.size(); i++) {
            Long bid = bids.get(i);
            logger.info("正在获取卡口ID：【{}】",bid);

            Map map = imageDownload.eventIds(bid, 1);
            List<Long> longs = (List<Long>)map.get("list");
            long count = (Long) map.get("count");

            for (Long eventId : longs) {
                List<String> c = imageDownload.downloadUrl(bid, eventId);

                for (String s : c) {
                    String paths = path + "/" + bid + "/" +""+ eventId +"/";
                    imageDownload.downloadImage(s,paths,s.substring(s.lastIndexOf("/")+1));
                }
            }

            logger.info("==========================================进入分页=====================");

            double pagesize = Math.ceil(count/10);
            for (int j = 2; j < pagesize; j++) {
                Map map2 = imageDownload.eventIds(bid, j);
                List<Long> longs2 = (List<Long>)map2.get("list");


                for (Long eventId : longs2) {
                    List<String> c = imageDownload.downloadUrl(bid, eventId);

                    for (String s : c) {
                        String paths = path + "/" + bid + "/" + eventId +"/";
                        imageDownload.downloadImage(s,paths,s.substring(s.lastIndexOf("/")+1));
                    }
                }
            }

        }

    }


    public void navigationBayonet() throws IOException {
        Call call = HttpUtil.get(url);
        JSONObject imgDataJson = HttpUtil.json(call);
        if ("1".equals(imgDataJson.getString("result"))){
            JSONObject data1 = imgDataJson.getJSONObject("data");
            JSONArray jsonArray1 = data1.getJSONArray("list");
            logger.info("数量 : [{}]",jsonArray1.size());
            if (jsonArray1.size() > 0) {
                for (int a = 0; a < jsonArray1.size(); a++) {
                    JSONObject jsonObject2 = (JSONObject) jsonArray1.get(a);

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



    //喜讯

    /**
     * 获取卡口ID
     */
    public List<Long> getBayonetIds() throws IOException {
        Call call = HttpUtil.get("http://198.18.101.49:8081/FsHsApi/getDetectingList");
        JSONObject json = HttpUtil.json(call);

        JSONArray data = json.getJSONArray("data");

        List<Long> rest =new ArrayList<Long>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject o = (JSONObject)data.get(i);

            rest.add(o.getLongValue("id"));

        }

        return rest;

    }

    /**
     *
     *获取事件id
     */
    public Map eventIds(Long mpId,int currentPage){


        logger.info("正在获取 [{}]卡口事件 第 [{}]页",mpId,currentPage);

        Call call = HttpUtil.get("http://198.18.101.49:8081/FsHsApi/getflowDetection?eventType=6&mpId="+mpId+"&pageSize=10&currentPage="+currentPage);
        List<Long> res = new ArrayList<Long>();
        Map<String, Object> re = new HashMap<String, Object>();

        try {
            JSONObject json = HttpUtil.json(call);

            JSONObject data = json.getJSONObject("data");

            long count = data.getLongValue("Count");
            JSONArray list = data.getJSONArray("List");
            for (int i = 0; i < list.size(); i++) {
                JSONObject o = (JSONObject)list.get(i);

                res.add(o.getLongValue("eventId"));
            }

            re.put("count",count);
            re.put("list",res);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("事件获取：{}",re);

        return re;
    }


    public List<String> downloadUrl(Long mpId,Long eventId ){
        Call call = HttpUtil.get("http://198.18.101.49:8081/FsHsApi/getflowDetectionImg?mpId="+mpId+"&eventId="+eventId);

        try {
            JSONObject json = HttpUtil.json(call);

            JSONArray data = json.getJSONArray("data");
            List<String> strings = new ArrayList<String>();
            if (data!=null){
                 strings = data.toJavaList(String.class);
            }

            return strings;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<String>();
    }


    public void downloadImage(String url,String path,String fileName){
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        logger.info("正在下载：[{}]",url);
        Call call = HttpUtil.get(url);
        HttpUtil.dowload(call,path,fileName);

    }
}
