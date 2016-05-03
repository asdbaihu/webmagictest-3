package webmagictest;

import java.util.List;

import com.jayway.jsonpath.JsonPath;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class Ttestfdsffs implements PageProcessor {

	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setUserAgent(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.22 Safari/537.36");

	public static void main(String[] args) {

		Spider.create(new Ttestfdsffs()).addUrl("http://v.pptv.com/show/dYJU0xhYQXiciaYMg.html")
				.addPipeline(new ConsolePipeline()).run();
	}

	public void process(Page page) {
		if (page.getUrl()
				.regex("http://v.pptv.com/show/videoList\\?from=web&version=1.0.0&format=jsonp&pid=\\d+&cat_id=\\d+&ppi=302c3332&vt=22&cb=pplive_callback_0")
				.match()) {
			System.out.println(page.getUrl());
			String jsonResult = page.getJson().regex("\\{.+\\}").toString();
			System.out.println(jsonResult);
			String sumpv1 = JsonPath.read(jsonResult, "$.data.pv");
			try {
				String sumpv = JsonPath.read(jsonResult, "$.data.pv");
				page.putField("each-playernum：", sumpv);
			} catch (Exception e) {
				System.out.println("eeeeeee");
			} finally {
				System.out.println("pass");
			}
			List<String> pvs = JsonPath.read(jsonResult, "$..pv");
			page.putField("playsum", pvs);

		} else {
			String jsonString = page.getHtml().xpath("//script[@type=text/javascript]").regex("var webcfg = \\{.+\\}")
					.regex("\\{.+\\}").toString();

			page.putField("webcfg", jsonString);
			String pid = JsonPath.read(jsonString, "$.pid").toString();
			String cat_id = JsonPath.read(jsonString, "$.cat_id").toString();
			String type = JsonPath.read(jsonString, "$.type").toString();
			page.putField("---", pid + " " + cat_id + " " + type);
			String url = "http://v.pptv.com/show/videoList?from=web&version=1.0.0&format=jsonp&pid=" + pid + "&cat_id="
					+ cat_id + "&ppi=302c3332&vt=22&cb=pplive_callback_0";
			page.addTargetRequest(url);
		}
	}

	public Site getSite() {
		return site;
	}

}
