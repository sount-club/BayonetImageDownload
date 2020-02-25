package cc.winfo.service.bayonet;

import cc.winfo.service.bayonet.entity.BayonetEvent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.animation.KeyFrame;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ImageDownload {

    static Logger logger = LoggerFactory.getLogger(ImageDownload.class);
    static String url = "http://198.18.127.220:80/crosssection/getShipDetailsList.action?pageSize=1000000&currentPage=1&hhuser=gz_dwjk&hhpwd=gz_dwjk";

    static String path = "E:/bImgs";

    Date startTime=null;

    Date endTime = null;
    public static void main(String[] args) throws IOException {
        ImageDownload imageDownload = new ImageDownload();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in ));
        System.out.print("输入开始时间：\n");
        String start = br.readLine();

        System.out.print("输入结束时间：\n");
        String end = br.readLine();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            imageDownload.startTime= format.parse(start);
            imageDownload.endTime= format.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        List<Long> bids = imageDownload.getBayonetIds();

        logger.info("卡口ID获取成功：[{}]",bids);

        for (int i = 0; i < bids.size(); i++) {
            Long bid = bids.get(i);
            logger.info("正在获取卡口ID：【{}】",bid);

            long pagesize = 2;
            for (int page = 1; page < pagesize; page++) {
                logger.info("正在处理分页数据 卡口ID：[{}]，第[{}]页",bid,page);

                Map eventMap = imageDownload.events(bid, page);

                if (page==1){
                    long count = (Long) eventMap.get("count");
                    pagesize = (count+9)/10;
                }

                List<BayonetEvent> events = (List<BayonetEvent>)eventMap.get("list");
                imageDownload.eventHandler(events,bid);

            }

        }

    }

    /**
     *
     * @param events 事件集合
     * @param bid 卡口编号
     */
    public void eventHandler(List<BayonetEvent> events,long bid){
        for (BayonetEvent event : events) {

            Date time = event.getTime();

            if (startTime!=null&& startTime.getTime()>time.getTime()){
                continue;
            }

            if (endTime!=null&& endTime.getTime()<time.getTime()){
                continue;
            }
            List<String> urls = this.downloadUrl(bid, event.getEventId());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String timeStr = format.format(time);

            for (String url : urls) {
                String paths = path + "/" + bid + "/" +timeStr+"/"+ event +"/";
                this.downloadImage(url,paths,url.substring(url.lastIndexOf("/")+1));
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
    public Map events(Long mpId, int currentPage){


        logger.info("正在获取 [{}]卡口事件 第 [{}]页",mpId,currentPage);

        Call call = HttpUtil.get("http://198.18.101.49:8081/FsHsApi/getflowDetection?eventType=6&mpId="+mpId+"&pageSize=10&currentPage="+currentPage);
        List<BayonetEvent> res = new ArrayList<BayonetEvent>();
        Map<String, Object> re = new HashMap<String, Object>();

        try {
            JSONObject json = HttpUtil.json(call);

            JSONObject data = json.getJSONObject("data");

            long count = data.getLongValue("Count");
            JSONArray list = data.getJSONArray("List");
            for (int i = 0; i < list.size(); i++) {
                JSONObject o = (JSONObject)list.get(i);
                BayonetEvent bayonetEvent = o.toJavaObject(BayonetEvent.class);
                res.add(bayonetEvent);
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
