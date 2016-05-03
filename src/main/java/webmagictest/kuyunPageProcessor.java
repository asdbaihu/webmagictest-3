package webmagictest;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;

public class kuyunPageProcessor implements PageProcessor {
	// 抓取网站的相关配置
	private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

	public Site getSite() {

		return site;
	}

	// 定制爬虫逻辑的核心接口，编写抽取逻辑
	public void process(Page page) {
		// page.getUrl()需要toString才能与字符串比较
		System.out.println(page.getUrl().toString().equals("http://eye.kuyun.com/api/tvlb"));
        //打印第一页的信息
		if (page.getUrl().toString().equals("http://eye.kuyun.com/api/tvlb")) {
			List<String> ids = new JsonPathSelector("$.data[*].id").selectList(page.getRawText());
			if (CollectionUtils.isNotEmpty(ids)) {
				for (String id : ids) {
					page.addTargetRequest("http://eye.kuyun.com/api/min_ratings?tv_id=" + id);
				}
			}
			// 全部
			page.putField("全部", new JsonPathSelector("$.data[*]").selectList(page.getRawText()));
			// 分类
			page.putField("id", new JsonPathSelector("$.data[*].id").selectList(page.getRawText()));
			page.putField("排名", new JsonPathSelector("$.data[*].rank").selectList(page.getRawText()));
			page.putField("频道", new JsonPathSelector("$.data[*].tv_name").selectList(page.getRawText()));
			page.putField("节目", new JsonPathSelector("$.data[*].epg_name").selectList(page.getRawText()));
			page.putField("市占率", new JsonPathSelector("$.data[*].market_ratings").selectList(page.getRawText()));
			page.putField("关注度", new JsonPathSelector("$.data[*].tv_ratings").selectList(page.getRawText()));
        //打印：：实时：：的页面的内容
		} else {
			page.putField("实时", new JsonPathSelector("$.data.list[*]").selectList(page.getRawText()));
		}

		

	}

	public static void main(String args[]) {
		Spider.create(new kuyunPageProcessor()).addUrl("http://eye.kuyun.com/api/tvlb").run();
	}

}
