package webmagictest;

import java.util.Scanner;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class searchBaofeng implements PageProcessor {
	// 配置
	Site site = Site.me().setRetryTimes(3).setSleepTime(0).setUserAgent(
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.22 Safari/537.36");

	public static void main(String[] args) {
		// 输入要查询的节目
		System.out.println("请输入要查找的节目");
		Scanner in = new Scanner(System.in);
		String inString = in.nextLine();
		// 是否为空
		if (!inString.equals("")) {
			String url = "";
			// 获取查询不到节目的异常

			try {
				Spider.create(new searchBaofeng()).addUrl(url).addPipeline(new ConsolePipeline()).run();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				System.out.println("查询不到该节目");
			}
		}
	}

	public void process(Page page) {

	}

	public Site getSite() {
		return site;
	}

}
