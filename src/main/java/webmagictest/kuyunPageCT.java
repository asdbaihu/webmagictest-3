package webmagictest;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;

public class kuyunPageCT implements PageProcessor {
	// 抓取网站的相关配置
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	public static void main(String[] args) {
		Spider.create(new kuyunPageCT()).addUrl("http://eye.kuyun.com/api/tvlb").run();
	}

	public void process(Page page) {

		
		// page.getUrl()需要toString才能与字符串比较
		System.out.println(page.getUrl().toString().equals("http://eye.kuyun.com/api/tvlb"));

		if (page.getUrl().toString().equals("http://eye.kuyun.com/api/tvlb")) {
			List<String> ids = new JsonPathSelector("$.data[*].id").selectList(page.getRawText());
			if (CollectionUtils.isNotEmpty(ids)) {
				for (String id : ids) {
					page.addTargetRequest("http://eye.kuyun.com/api/min_ratings?tv_id=" + id);
				}
			}

		} else {
			page.putField("实时", new JsonPathSelector("$.data.list[*]").selectList(page.getRawText()));
		}

	}

	public Site getSite() {
		return site;
	}

}
