package webmagictest;

import java.util.Date;
import java.util.Scanner;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class searchFengxing implements PageProcessor {
	private long programId = 0;
	// 配置
	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setUserAgent(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.22 Safari/537.36");

	public void process(Page page) {

		// 查询节目的结果，将最匹配的节目找出类，
		if (page.getUrl().regex("http://q1\\.fun\\.tv/api/search_hint\\?key=.+&uc=24&isajax=1").match()) {
			String jsonString = page.getJson().toString();

			String url = JsonPath.read(jsonString, "$.data[0].url").toString();
			if (url.equals("[]")) {
				System.out.println("风行没有收录该节目");
			} else {
				System.out.println(url);
				String newurl = "http://www.fun.tv" + url;
				page.addTargetRequest(newurl);
			}
			// 得到播放量
		} else if (page.getUrl().regex("http://www\\.fun\\.tv/.+").match()) {
			// 节目名
			String title = page.getHtml().xpath("//div[@class=sub-layout]/h3/a/text()").toString();
			// 从页面得到播放量
			String oldpv = page.getHtml().xpath("a[@class='exp-num']/text()").toString();
			String newpv = oldpv.replace(",", "");
			String pv = newpv.substring(3);

			// 返回一个json对象
			JSONObject info = new JSONObject();
			// 抓取时间
			Date time = new Date();
			info.put("collectDate", time);
			info.put("name", title);
			info.put("programId", programId);
			info.put("sourceId", 15);
			info.put("vvTotal", pv);
			// 播放总体数据
			page.putField("video_play_info", info);
		}

	}

	public Site getSite() {
		return site;
	}

	public searchFengxing setProgramId(long programId) {
		this.programId = programId;
		return this;
	}

	public static void main(String[] args) {
		// 输入要查询的节目
		System.out.println("请输入要查找的节目");
		Scanner in = new Scanner(System.in);
		String inString = in.nextLine();
		// 是否为空
		if (!inString.equals("")) {
			String url = "http://q1.fun.tv/api/search_hint?key=" + inString + "&uc=24&isajax=1";
			// 获取查询不到节目的异常
			try {
				Spider.create(new searchFengxing()).addUrl(url).addPipeline(new ConsolePipeline()).run();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
