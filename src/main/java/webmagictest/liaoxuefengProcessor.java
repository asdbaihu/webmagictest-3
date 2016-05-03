package webmagictest;

import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class liaoxuefengProcessor implements PageProcessor{
	//抓取网站的相关配置
    private Site site=Site.me().setRetryTimes(3).setSleepTime(1000);
    
	public static void main(String[] args) {
		Spider.create(new liaoxuefengProcessor()).addUrl("http://www.liaoxuefeng.com/wiki/0014316089557264a6b348958f449949df42a6d3a2e542c000")
		.addPipeline(new ConsolePipeline()).run();
	}

	public Site getSite() {
		return site;
	}

	public void process(Page page) {
		//目录
		page.putField("目录",page.getHtml().xpath("//div[@class=x-sidebar-left-content]/ul/li/a/text()").all());
		//增加链接
		List<String> urls=page.getHtml().links().regex("/wiki/0014316089557264a6b348958f449949df42a6d3a2e542c000/\\w+").all();
		page.addTargetRequests(urls);
		//内容
		page.putField("内容",page.getHtml().xpath("//div[@class=x-content]").toString());
	}

}
