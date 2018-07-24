package Qmusic;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 搜索结果获取解析类
 * @author 83588
 */
public class Search {
	/** json数据中的搜索关键词 */
	public String js_keyword;

	/** 当前搜索结果页页码 */
	public int js_curpage;

	/** 当前搜索结果页数据量(count) */
	public int js_curnum;

	/** 搜索结果总数据量 */
	public int js_totalnum;

	/** 搜索结果页总页码(count) */
	public int js_totalpage;

	/** 详细数据集 */
	public Media[] list;

	/** Search Result URL */
	private static String searchStatement = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?new_json=1&searchid=57841467114235795&t=0&aggr=1&cr=1&lossless=0&flag_qc=0&n=20&loginUin=0&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8";

	Search(String word, int page) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpUriRequest httpUrlRe = new HttpGet(searchStatement + "&w=" + word + "&p=" + page);
		CloseableHttpResponse response = httpClient.execute(httpUrlRe);
		if (response != null) {
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "UTF-8");
			//构造json解析器对json数据进行解析
			JsonParser jsParser = new JsonParser();
			JsonObject jsonOb = (JsonObject) jsParser.parse(result);
			jsonOb = (JsonObject) jsParser.parse(jsonOb.get("data").toString());
			js_keyword = jsonOb.get("keyword").getAsString();									//获得在json数据中的搜索关键词
			jsonOb = (JsonObject) jsParser.parse(jsonOb.get("song").toString());
			js_curpage = jsonOb.get("curpage").getAsInt();											//获得在json数据中的当前结果页页码
			js_totalnum = jsonOb.get("totalnum").getAsInt();										//获得在json数据中的所有搜索结果数据数量
			JsonArray jsArr = jsParser.parse(jsonOb.get("list").toString()).getAsJsonArray();
			js_curnum = jsArr.size();																				//计算当前搜索结果页数据数量
			//对搜索结果总页码进行计算
			if(js_curnum < 20) {
				js_totalpage = js_curpage;
			}
			else if (js_totalnum % js_curnum > 0) {
				js_totalpage = js_totalnum / js_curnum + 1;
			} else
				js_totalpage = js_totalnum / js_curnum;
			
			int i = 0;
			list = new Media[js_curnum];
			//用json返回数据中的list数组构造Media对象
			for (JsonElement js_ele : jsArr) {
				list[i] = new Media(js_ele);
				i++;
			}
		}
		if (response != null) {
			response.close();
		}
		if (httpClient != null) {
			httpClient.close();
		}
	}

	public void debugShow() {
		System.out.println("keyword: " + this.js_keyword);
		System.out.println("current Number: " + this.js_curnum);
		System.out.println("current Page: " + this.js_curpage);
		System.out.println("total Number: " + this.js_totalnum);
		System.out.println("total Page: " + this.js_totalpage);
		System.out.println();
		System.out.println("Song List Debug:");
		for (Media song : list) {
			System.out.println(song.singer + " - " + song.title);
		}
	}

	public static void main(String agrs[])
			throws ClientProtocolException, IOException, UnsupportedAudioFileException, LineUnavailableException {
		Search search = new Search("病变", 2);
		search.debugShow();
		search.list[0].debugShow();
		//AudioPlay.urlPlay(search.list[0].getMeidaUrl(),	search.list[0].singer.replaceAll(" / ", "&") + " - " + search.list[0].title + ".m4a");

	}
}