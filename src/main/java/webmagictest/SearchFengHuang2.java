package webmagictest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.AfterExtractor;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class SearchFengHuang2 implements PageProcessor{
	private Integer playnum = 0;
	private Integer commentnum = 0;
	private Integer dingnum = 0;
	private Integer cainum = 0;
	private static List<String> urlList = new ArrayList<String>();
	private static List<String> namesList = new ArrayList<String>();
	//节目编号
	private long programId=0;
	
	//节目名
	private String name="";

	// 配置
	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setCharset("UTF-8").setUserAgent(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.22 Safari/537.36");

	public static void main(String[] args) {
		// 先将综艺节目目录 节目名 和地址存到map里面
		System.out.println("请等待程序准备。。。。。");
		Spider.create(new SearchFengHuang2()).addUrl("http://v.ifeng.com/vlist/nav/vari/1/list.shtml")
				.addPipeline(new ConsolePipeline()).run();
				// System.out.println(urlList);

		// 输入要查询的节目

		System.out.println("请输入要查找的节目");
		Scanner in = new Scanner(System.in);
		String inString = in.nextLine();
		if (namesList.contains(inString)) {
			String url = urlList.get(namesList.indexOf(inString));
			Spider.create(new SearchFengHuang2()).addUrl(url).addPipeline(new ConsolePipeline()).run();
			
			/*name=inString;
			Page page=new Page();
			//输出json对象
			JSONObject info = new JSONObject();
			//抓取时间
			Date time=new Date();
			info.put("collectDate", time);
			info.put("name", name);
			info.put("programId", 0);
			info.put("sourceId", 13);
			info.put("vvTotal", playnum);
			//播放总体数据
            page.putField("video_play_info",info);
            System.out.println(info);*/
		} else {
			System.out.println("没有找到这个节目");
		}
	}

	public void process(Page page) {
	       
		// 取综艺节目的节目单和对应链接
		if (page.getUrl().regex("http://v\\.ifeng\\.com/vlist/nav/vari/\\d/list\\.shtml").match()) {
			// 节目链接
			List<String> urls = page.getHtml().xpath("//ul[@id='list_vari']/li/h6").links().all();
			urlList.addAll(urls);
			// 节目名字
			List<String> names = page.getHtml().xpath("//ul[@id='list_vari']/li/h6/a/text()").all();
			namesList.addAll(names);
			// 将下一页节目单的链接加入待抓页面
			List<String> allpageurl = page.getHtml().xpath("//div[@class='vlistbox']/div[@class='page']").links().all();
			String nextpageurl = allpageurl.get(allpageurl.size() - 1);
			if (!nextpageurl.equals(null)) {
				page.addTargetRequest(nextpageurl);
			}
		}

		// 取数据
		if (page.getUrl().regex("http://v\\.ifeng\\.com/vlist/.+/detail\\.(shtml)$").match()) {

			List<String> urllist = page.getHtml().xpath("//div[@class='picstyle01']/ul/li/h6").links().all();
			// System.out.println(urllist);
			page.addTargetRequests(urllist);
			// 将下一页节目单的链接加入待抓页面
			List<String> allpageurl = page.getHtml().xpath("//div[@class='picstyle01']/div[@class='page']").links()
					.all();
			String nextpageurl = allpageurl.get(allpageurl.size() - 1);
			if (!nextpageurl.equals(null)) {
				page.addTargetRequest(nextpageurl);
			}
			// 播放页面得到获取数据的链接
		} else if (page.getUrl().regex("http://v\\.ifeng\\.com/.+\\.(shtml)$").match()
				&& (!page.getUrl().regex("http://v\\.ifeng\\.com/vlist/.+/detail\\.(shtml)$").match())
				&& (!page.getUrl().regex("http://v\\.ifeng\\.com/vlist/nav/vari/\\d/list\\.shtml").match())) {
			// 播放序列
			String uniquestr1 = page.getUrl().toString().split("/")[page.getUrl().toString().split("/").length - 1];
			String uniquestr = uniquestr1.split("\\.")[0];
			// 根据播放序列得到请求数据的地址
			// 播放量地址
			String playnumurl = "http://survey.news.ifeng.com/getaccumulator_weight.php?format=js&serverid=2&key="
					+ uniquestr;
			// 评论数地址
			String commentnumurl = "http://comment.ifeng.com/getv.php?job=3&format=js&docurl="
					+ page.getUrl().toString() + "&";
			// 顶地址
			String dingnumurl = "http://survey.news.ifeng.com/getaccumulator_ext.php?key=" + uniquestr
					+ "ding&format=js&serverid=1&var=ding";
			// 踩地址
			String cainumurl = "http://survey.news.ifeng.com/getaccumulator_ext.php?key=" + uniquestr
					+ "cai&format=js&serverid=1&var=cai";
			// 加入地址
			page.addTargetRequest(playnumurl);
			page.addTargetRequest(commentnumurl);
			page.addTargetRequest(dingnumurl);
			page.addTargetRequest(cainumurl);
			// 取播放量
		} else if (page.getUrl()
				.regex("http://survey\\.news\\.ifeng\\.com/getaccumulator_weight\\.php\\?format=js&serverid=2&key=.+")
				.match()) {
			String json = page.getJson().regex("\\{.+\\}").toString();
			Integer num = JsonPath.read(json, "$.browse");
			playnum += num;
			// 评论数
		} else if (page.getUrl().regex("http://comment\\.ifeng\\.com/getv\\.php\\?job=3&format=js&docurl=.+").match()) {
			String json = page.getJson().regex("\\d+").toString();
			Integer num = Integer.parseInt(json);
			commentnum += num;
			// 顶数量
		} else if (page.getUrl()
				.regex("http://survey\\.news\\.ifeng\\.com/getaccumulator_ext\\.php\\?key=.+ding&format=js&serverid=1&var=ding")
				.match()) {
			String json = page.getJson().regex("\\{.+\\}").toString();
			Integer num = Integer.parseInt(JsonPath.read(json, "$.browse").toString());
			dingnum += num;
			// 踩数量
		} else if (page.getUrl()
				.regex("http://survey\\.news\\.ifeng\\.com/getaccumulator_ext\\.php\\?key=.+cai&format=js&serverid=1&var=cai")
				.match()) {
			String json = page.getJson().regex("\\{.+\\}").toString();
			Integer num = Integer.parseInt(JsonPath.read(json, "$.browse").toString());
			cainum += num;
			
		   
		}

	}

	public Site getSite() {
		return site;
	}
	
	
	public SearchFengHuang2 setProgramId(long programId){
		this.programId=programId;
		return this;
	}
}
