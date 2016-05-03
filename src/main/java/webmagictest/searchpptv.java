package webmagictest;

import java.util.Date;
import java.util.Scanner;

import com.alibaba.fastjson.JSONObject;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import com.jayway.jsonpath.JsonPath;

public class searchpptv implements PageProcessor {
	private long programId = 0;
	// 配置
	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setUserAgent(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.22 Safari/537.36");

	public void process(Page page) {
		// 判断页面地址
		if (page.getUrl()
				.regex("http://searchapi\\.pptv\\.com/query/nt\\?q=.+&cm=ikan&colordis=red&hasVirtual=1&vipdis=0&fm=32&cnt=10")
				.match()) {
            			
			String id= page.getJson().jsonPath("$[1][0].channelId").toString();
			// 获取json对象中的id
			try {
				//String id = JsonPath.read(jsonResult, "$[1][0].channelId").toString();
				System.out.println("id:" + id);
				// 根据查询第一条的id，添加到待抓取页面
				String url = "http://epg.api.pptv.com/detail.api?cb=recDetailData&auth=noauth&vid=" + id
						+ "&mode=onlyset&virtual=1&series=1&platform=ikan&userLevel=0";
				page.addTargetRequest(url);
			} catch (Exception e) {
				System.out.println("没有该节目的信息");
			}
		} else if (page.getUrl()
				.regex("http://epg\\.api\\.pptv\\.com/detail\\.api\\?cb=recDetailData&auth=noauth&vid=\\d+&mode=onlyset&virtual=1&series=1&platform=ikan&userLevel=0")
				.match()) {

			String jsonResult = page.getJson().regex("\\{.+\\}").toString();
			
			
			// 节目名
			String name = JsonPath.read(jsonResult, "$.v.title").toString();
			// 总播放量
			String pv = JsonPath.read(jsonResult, "$.v.pv").toString();
			// 节目在系统中的id
			String id = JsonPath.read(jsonResult, "$.v.vsEpId").toString();
			// json 对象
			JSONObject info = new JSONObject();
			// 时间
			Date time = new Date();
			info.put("collectDate", time);
			info.put("name", name);
			info.put("programId", programId);
			info.put("sourceId", 16);
			info.put("vvTotal", pv);
			// 播放总体数据
			page.putField("video_play_info", info);
		}
	}

	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		// 输入要查询的节目
		System.out.println("请输入要查找的节目");
		Scanner in = new Scanner(System.in);
		String inString = in.nextLine();
		// 是否为空
		if (!inString.equals("")) {
			String url = "http://searchapi.pptv.com/query/nt?q=" + inString
					+ "&cm=ikan&colordis=red&hasVirtual=1&vipdis=0&fm=32&cnt=10";

			Spider.create(new searchpptv()).addUrl(url).addPipeline(new ConsolePipeline()).run();
		}
	}

	public searchpptv setProgramId(long programId) {
		this.programId = programId;
		return this;
	}

}
