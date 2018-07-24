# QQmusicDownloader
一份JAVA课设，懒得写README了，直接复制课程报告:
## 第1章 程序概述
程序功能：能返回关键词在QQ音乐网页上的搜索结果，并提供绕过QQ音乐客户端下载和播放搜索结果中的音乐功能。

实现所需主要技术：

    通过http client 抓取网页数据
    
		通过gson对json数据解析处理
    
		通过swing设计程序界面
    
程序界面设计：

 ![MainUIinterface](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/MainUIinterface.png "MainUIinterface")



## 第2章 程序设计
### 2.1搜索结果数据抓取分析设计
在QQ音乐网站页面搜索歌曲
  ![SearchInterface](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/Search.png)

对搜索过程中的各个请求资源信息分析发现
	返回的搜索数据结果在client_search_cp?ct=24&qqmusic_ver=129… 资源中，并且是以json数据返回
  
 ![SearchParse](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/searchParse.png)
	分析发现通过控制URL中的 w字段和p字段可以实现获取不同的搜索结果以及页数

### 2.2音乐信息抓取分析设计
	进入在线播放页面，对播放过程中的各个请求资源信息分析发现
  
	返回的用于验证的vkey字符串数据在fcg_music_express_mobile3.fcg?g_tk… 资源中，并且是以json数据返回
  
  ![playParse1](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/playParse.png)

  ![PlayParse2](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/playParse2.png)
 
 
	分析发现通过控制URL中的 filename字段和songmid字段可以实现获取不同音乐文件的vkey字符串数据
  
	然后通过结合音乐的vkey和filename，设置qqmusic_fromtag cookie信息请求dl.stream.qqmusic.qq.com网站可以获得音乐文件数据
  
  ![PlayParse3](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/playParse3.png)
  
  
## 第3章 程序实现与测试
### 3.1 搜索结果数据抓取分析实现
````
   CloseableHttpClient httpClient = HttpClients.createDefault();
   HttpUriRequest httpUrlRe = new HttpGet(searchStatement + "&w=" + word + "&p=" + page);
   CloseableHttpResponse response = httpClient.execute(httpUrlRe);
   if (response != null) {
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity, "UTF-8");
		JsonParser jsParser = new JsonParser(); 					//构造解析器准备解析数据
		JsonObject jsonOb = (JsonObject) jsParser.parse(result);
		jsonOb = (JsonObject) jsParser.parse(jsonOb.get("data").toString());
		js_keyword = jsonOb.get("keyword").getAsString();			//获得json数据中的搜索关键词
		jsonOb = (JsonObject) jsParser.parse(jsonOb.get("song").toString());
		js_curpage = jsonOb.get("curpage").getAsInt();				//获得json数据中的当前搜索结果页页码
		js_totalnum = jsonOb.get("totalnum").getAsInt();			//获得json数据中的总搜索结果数量
		//获得当前搜索结果页音乐数据
		JsonArray jsArr = jsParser.parse(jsonOb.get("list").toString()).getAsJsonArray();	
		js_curnum = jsArr.size();							//统计当前搜索结果数量
		if (js_totalnum % js_curnum > 0) {
			js_totalpage = js_totalnum / js_curnum + 1;
		} else
			js_totalpage = js_totalnum / js_curnum;
		int i = 0;
		list = new Media[js_curnum];
		//对搜索结果进行分析处理
		for (JsonElement js_ele : jsArr) {
			list[i] = new Media(js_ele);
			i++;
			}
	}
````

### 3.2 音乐信息抓取分析实现
	//初始化歌曲信息
	JsonParser jsParser = new JsonParser();					//构造解析器准备解析数据
	JsonObject jsonOb = (JsonObject) jsParser.parse(data.toString());
	title = jsonOb.get("title").getAsString();					//获得主歌名
	JsonArray singerArr = new JsonArray();					//定义json数组解析歌手信息
	singerArr = jsonOb.get("singer").getAsJsonArray();
	singer = "";
	for(JsonElement js_singer : singerArr) {
		singer += (js_singer.getAsJsonObject().get("name") + " / ");
	}
	singer = singer.replaceAll("\"| / $", "");				//歌手信息分析处理
	
	//构造音乐文件名(用于获取vkey)
	filename = "C400" + jsonOb.get("file").getAsJsonObject().get("media_mid") + ".m4a";			
	filename = filename.replaceAll("\"", "");
	songmid = jsonOb.get("mid").getAsString();			//获得音乐songmid(用于获取vkey)
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
### 3.3 音乐文件下载播放实现
	System.out.println("Selected: " + songList.getSelectedIndex() + " ");			//输出被选中的音乐文件序号(用于调试)
	search.list[songList.getSelectedIndex()].debugShow();					//输出被选中的音乐文件信息(用于调试)

	//定义文件下载路径
	String uri = System.getProperty("user.dir") + "\\Download\\"
			+ search.list[songList.getSelectedIndex()].singer.replace(" / ", ",") + "-"
			+ search.list[songList.getSelectedIndex()].title + ".m4a";
	System.out.println("Temp directory: " + uri);						//输出文件下载路径(用于调试)
	File file = new File(uri);
	//测试路径是否存在，不存在则创建
	if (!file.getParentFile().exists()) {
		file.getParentFile().mkdirs();
	}
	if (!file.exists()) {
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	OutputStream output = null;
	BufferedOutputStream bufferedOutput = null;
		try {
			output = new FileOutputStream(file);
			bufferedOutput = new BufferedOutputStream(output);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			//通过HttpGet下载音乐文件数据
			HttpUriRequest httpUrlRe = new HttpGet(search.list[songList.getSelectedIndex()].getMeidaUrl());
			//设计cookie绕过验证
			httpUrlRe.addHeader(new BasicHeader("Cookie", "qqmusic_fromtag=66"));
			try {
				CloseableHttpResponse response = httpClient.execute(httpUrlRe);
				if (response != null) {
					HttpEntity entity = response.getEntity();
					bufferedOutput.write(EntityUtils.toByteArray(entity));
					bufferedOutput.close();
					Runtime ru = Runtime.getRuntime();
					//调用wmplayer播放音乐文件
					ru.exec("C://Program Files//Windows Media Player//wmplayer.exe \"" + uri + "\"");
				}
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
### 3.4 程序主要UI的实现
````
//定义jlist用于显示音乐列表
JList<String> songList = new JList();
songList.setBounds(40, 78, 377, 129);
mainPane.add(songList);
//定义JScrollPane用于控制音乐列表滚动条
JScrollPane scrollPane = new JScrollPane(songList);
scrollPane.setBounds(40, 80, 560, 250);
mainPane.add(scrollPane);
...
//获取搜索结果
search = new Search(textKeyword.getText(), 1);
search.debugShow();			//输出搜索结果主要信息(用于调试)
//定义搜索结果数据数组
String listData[] = new String[search.js_curnum];
int i = 0;
//遍历list对象数据存放音乐歌手歌名数据至listData数组
for (Media song : search.list) {
	listData[i] = song.singer + " - " + song.title;
	i++;
}
countLabel.setText("list: " + String.valueOf(search.js_curnum));		songList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//以数组形式设置jlist选项数据
songList.setListData(listData);
````

### 3.5程序测试
搜索歌名“不仅仅是喜欢”(该歌曲在客户端需要付费才可以下载)
成功返回搜索结果： 

![test1](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/test1.png)
 

选中第一首

![test2](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/test2.png)
 

下载播放[成功]

![test3](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/test3.png)


显示下一页搜索结果
成功返回数据：

![test4](https://github.com/easyjack/QQmusicDownloader/blob/master/pic/test4.png)

## 第4章 总结
### 4.1 存在的不足
由于找不到合适的可用于java解析m4a音频文件数据的方法，所以只能通过调用外部wmplayer程序播放音乐文件

 

