package com.vitasoy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadFtp {

    public static final int cache = 10*1024 ;

    public static String getResponse(String url) {
        String result = "";
        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;

            try {
                HttpGet httpGet = new HttpGet(url);
                client = HttpClients.createDefault();
                response = client.execute(httpGet);

                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<FileLocation> readFileLocationFromConfig(String configPath){
        List<FileLocation> files = new ArrayList<FileLocation>();
        try{
            String fileName = configPath;
            File file = new File(fileName);
            if(file.isFile() && file.exists()){
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
                BufferedReader bReader = new BufferedReader(reader);
                String lineText = "";
                while((lineText = bReader.readLine())!= null){
                    String[] fileLocation = lineText.split(";");
                    files.add(new FileLocation(fileLocation[0], fileLocation[1], fileLocation[2]));
                }
                reader.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return files;
    }

    public static String download(String url, String filepath){
        try {
            HttpClient client = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = client.execute(httpget);

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();

            File file = new File(filepath);
            file.getParentFile().mkdirs();
            FileOutputStream fileout = new FileOutputStream(file);
            /**
             * 根据实际运行效果 设置缓冲区大小
             */
            byte[] buffer = new byte[cache];
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileout.write(buffer, 0, ch);
            }
            is.close();
            fileout.flush();
            fileout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isDateWithinOneWeek(Date date) {
        Date now = new Date();
        return (now.getTime() - date.getTime()) <= 7 * 24 * 3600 * 1000;
    }

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("input config file path");
        }
     //   String filePath = "/Users/gongthomas/Workspace/github/readFtp/src/main/java/com/vitasoy/config.txt";
        String filePath = args[0];
        List<FileLocation> districts = readFileLocationFromConfig(filePath);
        for (FileLocation fileLocation : districts) {
            String result = getResponse(fileLocation.getUrl());
            String[] res = result.split("<br>");
            for (String oneLine : res) {
                if (oneLine.contains("HREF") && oneLine.contains("xlsx")) {
                    //2020/6/15    13:18       107183 <A HREF="/300POSKA/POSKA_Eastern_District/300poska@2020-06-08@2020-06-14.xlsx">300poska@2020-06-08@2020-06-14.xlsx</A>
                    String record = oneLine.trim();
                    //2020/6/15
                    String timeString = record.split(" ")[0].trim();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    Date date = null;
                    try {
                        date = sdf.parse(timeString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (date != null && isDateWithinOneWeek(date)) {
                        //300poska@2020-06-08@2020-06-14.xlsx</A>
                        String[] files = record.split("\">");
                        //300poska@2020-06-08@2020-06-14.xlsx
                        String fileName = files[files.length - 1].split("<")[0];
                        String fileUrl = fileLocation.getUrl().concat(fileName);
                        System.out.println(fileUrl);
                        System.out.println(record);
                        download(fileUrl, fileLocation.getFilePath().concat(fileName));
                    }
                }
            }
        }
    }
}
