package webmagictest;

import java.util.List;
import java.util.Scanner;

import com.jayway.jsonpath.JsonPath;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class searchFenghuang implements PageProcessor {
	private static Integer playnum = 0;
	private static Integer commentnum = 0;
	private static Integer dingnum = 0;
	private static Integer cainum = 0;

	// 配置
	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setCharset("UTF-8").setUserAgent(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.22 Safari/537.36");

	public static void main(String[] args) {
		// 输入要查询的节目
		System.out.println("请输入要查找的节目");
		Scanner in = new Scanner(System.in);
		String inString = in.nextLine();
		// 将输入的汉字提取拼音首字母
		ChineseCharToEn ccToEn = new ChineseCharToEn();
		String ins = ccToEn.getAllFirstLetter(inString);

		// 是否为空
		if (!inString.equals("")) {
			String url = "http://v.ifeng.com/api/search/columnData/" + ins + ".js?_=";

			Spider.create(new searchFenghuang()).addUrl(url).addPipeline(new ConsolePipeline()).run();

		}

	}

	public void process(Page page) {
		// 搜索节目的结果
		if (page.getUrl().regex("http://v\\.ifeng\\.com/api/search/columnData/.+\\.js\\?_=").match()) {
			// 提取出json对象
			String json = page.getJson().regex("= \\{.+\\};SO").regex("\\{.+\\}").toString();
			// 判断是否有该节目的节目单
			if (json.equals(null)) {
				System.out.println("没有该节目");
			} else {
				System.out.println(json);
				String listUrl = JsonPath.read(json, "$.listUrl");
				String title=JsonPath.read(json,"$columnTitle");
				System.out.println("节目名："+title);
				// 是否为一系列节目，或者单个节目
				if (listUrl.matches("http://v\\.ifeng\\.com/vlist/.+detail\\.shtml")) {
					page.addTargetRequest(listUrl);

				} else {

				}

			}
			// 从系列节目单中得到每个播放页面的地址，并且将后续页面(下一页)加入待抓页面
		} else if (page.getUrl().regex("http://v\\.ifeng\\.com/vlist/.+/detail\\.(shtml)$").match()) {

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
				&& (!page.getUrl().regex("http://v\\.ifeng\\.com/vlist/.+/detail\\.(shtml)$").match())) {
			// 播放序列
			String uniquestr1 = page.getUrl().toString().split("/")[page.getUrl().toString().split("/").length - 1];
			String uniquestr = uniquestr1.split("\\.")[0];
			// 根据播放序列得到请求数据的地址
			// 播放量地址
			String playnumurl = "http://survey.news.ifeng.com/getaccumulator_weight.php?format=js&serverid=2&key="
					+ uniquestr;
			// 评论数地址
			String commentnumurl = "http://comment.ifeng.com/getv.php?job=3&format=js&docurl="
					+ page.getUrl().toString()+"&";
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
			System.out.println("播放数：" + playnum);
			//评论数
		} else if (page.getUrl()
				.regex("http://comment\\.ifeng\\.com/getv\\.php\\?job=3&format=js&docurl=.+")
				.match()) {
			String json = page.getJson().regex("\\d+").toString();
			Integer num=Integer.parseInt(json);
			commentnum+=num;
			System.out.println("评论数："+commentnum);
			//顶数量
		} else if (page.getUrl()
				.regex("http://survey\\.news\\.ifeng\\.com/getaccumulator_ext\\.php\\?key=.+ding&format=js&serverid=1&var=ding")
				.match()) {
			String json = page.getJson().regex("\\{.+\\}").toString();
			Integer  num = Integer.parseInt(JsonPath.read(json, "$.browse").toString());
			dingnum += num;
			System.out.println("顶：" + dingnum);
			//踩数量
		} else if (page.getUrl()
				.regex("http://survey\\.news\\.ifeng\\.com/getaccumulator_ext\\.php\\?key=.+cai&format=js&serverid=1&var=cai")
				.match()) {
			String json = page.getJson().regex("\\{.+\\}").toString();
			Integer  num = Integer.parseInt(JsonPath.read(json, "$.browse").toString());
			cainum += num;
			System.out.println("踩：" + cainum);
		} 

	}

	public Site getSite() {
		return site;
	}

}
