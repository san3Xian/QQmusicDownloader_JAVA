package Qmusic;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 歌曲信息解析类
 * @author WILO
 */
public class Media {
	/**js返回歌名*/
	public String title;
	/**js返回歌手*/
	public String singer;
	/**js返回vkey*/
	public String vkey;
	/**js返回filename*/
	public String filename;
	/**js返回song mid*/
	public String songmid;
	/**获取vkey地址*/
	private static String getVkeyUrl = "https://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?loginUin=0&format=json&inCharset=utf8&outCharset=utf-8&cid=205361747&guid=0";
	
	public Media(JsonElement data) throws ClientProtocolException, IOException{
		//初始化歌曲信息
		JsonParser jsParser = new JsonParser();																	//构造解析器准备解析数据
		JsonObject jsonOb = (JsonObject) jsParser.parse(data.toString());
		title = jsonOb.get("title").getAsString();																	//获得主歌名
		JsonArray singerArr = new JsonArray();																	//定义json数组解析歌手信息
		singerArr = jsonOb.get("singer").getAsJsonArray();
		singer = "";
		for(JsonElement js_singer : singerArr) {
			singer += (js_singer.getAsJsonObject().get("name") + " / ");
		}
		singer = singer.replaceAll("\"| / $", "");																	//歌手信息分析处理
		filename = "C400" + jsonOb.get("file").getAsJsonObject().get("media_mid") + ".m4a";			//构造音乐文件名(用于获取vkey)
		filename = filename.replaceAll("\"", "");
		songmid = jsonOb.get("mid").getAsString();																			//获得音乐songmid(用于获取vkey)
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpUriRequest httpUrlRe = new HttpGet(getVkeyUrl + "&filename=" + filename + "&songmid=" + songmid);
		CloseableHttpResponse response = httpClient.execute(httpUrlRe);
		if(response != null){
			HttpEntity entity = response.getEntity();			
			String result = EntityUtils.toString(entity, "UTF-8");
			jsonOb = (JsonObject)jsParser.parse(result);
			//从json数据中提取vkey
			vkey = jsonOb.get("data").getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject().get("vkey").getAsString();
		}
		if (response != null){
            response.close();
        }
        if (httpClient != null){
            httpClient.close();
        }
	}
	
	public void debugShow() {
		System.out.println("title: " + title);
		System.out.println("singer: " + singer);
		System.out.println("filename: " + filename);
		System.out.println("songmid: " + songmid);
		System.out.println("playUrl: " + getMeidaUrl());
	}
	
	/**
	 * 构造音乐文件直连地址
	 */
	public String getMeidaUrl() {
		String url = "http://dl.stream.qqmusic.qq.com/";
		url = url + filename + "?vkey=" + vkey + "&guid=0&uin=0";
        return url;
	}
}
