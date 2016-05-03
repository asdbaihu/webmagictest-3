package webmagictest;

import java.util.List;

import com.jayway.jsonpath.JsonPath;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class mgtvProcessor implements PageProcessor {
	private static final String type_url = "http://list\\.pptv\\.com\\?type=\\d+";

	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setUserAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

	public static void main(String[] args) {

		Spider.create(new mgtvProcessor()).addUrl("http://list.pptv.com/").addPipeline(new ConsolePipeline()).addPipeline(new FilePipeline("D:\\webmagic\\")).thread(20)
				.run();
	}

	public void process(Page page) {
		// 所有的分类视频
		if (page.getUrl().toString().equals("http://list.pptv.com/")) {
			List<String> urls = page.getHtml().xpath("//div[@class=detail_menu]/ul/li").links().all();
			page.addTargetRequests(urls);
			// 获取当前分类的所有页面的get请求，加入待抓取列表
		} else if (page.getUrl().regex(type_url).match()) {
			String type = page.getUrl().toString().split("=")[1];
			int pageNum = Integer.parseInt(page.getHtml().xpath("//p[@class=pageNum]/text()").toString().split(" ")[1]);
			// System.out.println(type+" "+pageNum);
			for (int i = 1; i <= pageNum; i++) {
				page.addTargetRequest("http://list.pptv.com/channel_list.html?page=" + i + "&type=" + type);
			}

			// 分类特例：体育分类-------将所有页面的体育get请求 加入待抓列表
		} else if (page.getUrl().toString().equals("http://list.pptv.com/sports.html")) {
			System.out.println("sports");
			int pageNum = Integer.parseInt(page.getHtml().xpath("//p[@class=pageNum]/text()").toString().split(" ")[1]);
			for (int i = 1; i <= pageNum; i++) {
				page.addTargetRequest("http://list.pptv.com/sports/channel_list.html?page=" + i + "&");
			}
			// 分类下的每个视频信息
		} else if (page.getUrl().regex("http://list.pptv.com/(sports/)?channel_list.html\\?page=").match()) {
			List<String> urls = page.getHtml().xpath("//li").links().regex("http://v.pptv.com/show/.+").all();
			page.addTargetRequests(urls);
		//每个视频的播放量/人气	get请求地址
		}else if(page.getUrl().regex("http://v.pptv.com/show/.+").match()){
			String jsonString=page.getHtml().xpath("//script[@type=text/javascript]").regex("var webcfg = \\{.+\\}").regex("\\{.+\\}").toString();
			
			//page.putField("webcfg", jsonString);
			
			String pid=JsonPath.read(jsonString,"$.pid").toString();
			String  cat_id=JsonPath.read(jsonString,"$.cat_id").toString();
			String type=JsonPath.read(jsonString,"$.type").toString();
			//page.putField("---",pid+" "+cat_id+" "+type);
			String url="http://v.pptv.com/show/videoList?from=web&version=1.0.0&format=jsonp&pid="+pid+
					"&cat_id="+cat_id+"&ppi=302c3332&vt=22&cb=pplive_callback_0";
			page.addTargetRequest(url);
		//get请求，得到 播放量
		}else if(page.getUrl().regex("http://v.pptv.com/show/videoList\\?from=web&version=1.0.0&format=jsonp&pid=\\d+&cat_id=\\d+&ppi=302c3332&vt=22&cb=pplive_callback_0").match()){
			String jsonResult=page.getJson().regex("\\{.+\\}").toString();
			try {
				String sumpv=JsonPath.read(jsonResult,"$.data.pv");
				page.putField("each-playernum：",sumpv);
			} catch (Exception e) {
				System.out.println("-----------------发生异常-------------------");
			}finally {
				System.out.println("pass");
			}
			List<String> pvs=JsonPath.read(jsonResult,"$..pv");
			page.putField("playsum",pvs);
		}

	}

	public Site getSite() {
		return site;
	}

}
