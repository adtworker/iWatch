package com.adtworker.mail;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.net.Uri;
import android.util.Log;

import com.adtworker.mail.service.entity.ImgInfo;

public class BaiduImage {
	public final static Integer HTTP_RESPONSE_STATUS_SUCCESS_CODE = 200;
	/**
	 * 百度图片抓取url word-搜索关键词<br/>
	 * pn-起始页 <br/>
	 * rn-分页大小 <br/>
	 * width-指定宽<br/>
	 * height-指定高<br/>
	 */
	static String REQUEST_URL_TEMPLETE = "http://image.baidu.com/i?ct=201326592&lm=-1&tn=baiduimagenojs&pv=&word={0}&z=10&pn={1}&rn={2}&cl=2&width={3}&height={4}";
	static String BAIDU_IMG_URL_PREFIX = "http://image.baidu.com";

	/**
	 * 抓取给定url转化为string
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getHTML(String url) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
		if (HTTP_RESPONSE_STATUS_SUCCESS_CODE
				.equals(statusLine.getStatusCode())) {
			String result = EntityUtils.toString(response.getEntity());
			return result;
		} else {
			return "";
		}
	}

	/**
	 * 加载指定URL，通过指定xpath获取对应元素
	 * 
	 * @param url
	 * @param xpath
	 * @return 包含对应元素的结点集合，可能为null
	 * @throws Exception
	 */
	public static Object[] getNode(String url, String xpath) throws Exception {
		CleanerProperties props = new CleanerProperties();

		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(new URL(url));
		// getHTML方法比较慢,带测试
		// TagNode tagNode = new HtmlCleaner(props).clean(getHTML(url));
		Object[] ns = tagNode.evaluateXPath(xpath);
		return ns;
	}

	public static List<ImgInfo> getImgInfoList(String keyword, int pageNumber,
			int size, int width, int height) throws Exception {
		List<String> imgUrlList = getImgUrl(keyword, pageNumber, size, width,
				height);
		List<ImgInfo> imgList = new ArrayList<ImgInfo>();
		for (int i = 0; i < imgUrlList.size(); i++) {
			ImgInfo info = new ImgInfo(imgUrlList.get(i));
			imgList.add(info);
		}
		return imgList;
	}

	/**
	 * 获取img url
	 * 
	 * @param keyword
	 *            搜索关键词
	 * @param pageNumber
	 *            起始页
	 * @param size
	 *            分页大小
	 * @return img 绝对路径
	 * @throws Exception
	 */
	public static List<String> getImgUrl(String keyword, int pageNumber,
			int size, int width, int height) throws Exception {
		String gbkKeyword = Uri.encode(keyword, "GBK");
		List<String> imgUrlList = new ArrayList<String>();
		Random random = new Random(System.currentTimeMillis());
		Integer randomPageNumber = random.nextInt(300);
		String requestUrl = MessageFormat.format(REQUEST_URL_TEMPLETE,
				gbkKeyword, randomPageNumber, size, width, height);
		Log.d("ImageManager", requestUrl);
		Object[] nodes = getNode(requestUrl, "//td/a");
		if (nodes != null && nodes.length > 0) {
			for (int i = 0; i < nodes.length; i++) {
				TagNode node = (TagNode) nodes[i];
				String detailInfoURL = BAIDU_IMG_URL_PREFIX
						+ node.getAttributeByName("href");
				Object[] detailNodes = getNode(detailInfoURL, "//table//img");
				if (detailNodes != null && detailNodes.length > 0) {
					TagNode detailInfo = (TagNode) detailNodes[0];
					String imgUrl = detailInfo.getAttributeByName("src");
					Log.d("ImageManager", imgUrl);
					imgUrlList.add(imgUrl);
				}
			}
		}
		return imgUrlList;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Calendar.getInstance().getTime());
		List<String> imgList = getImgUrl("HM", 1, 7, 480, 800);
		for (int i = 0; i < imgList.size(); i++) {
			System.out.println(imgList.get(i));
		}
		System.out.println(Calendar.getInstance().getTime());
	}
}
