package dotasource.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

import dotasource.model.Like;
import dotasource.model.LikeTable;
import dotasource.model.Notification;
import dotasource.model.User;

public class DotasourceManager {

	public static final String COOKIE_USER_ID = "wcf21_userID";
	public static final String COOKIE_PASSWORD = "wcf21_password";
	public static final String COOKIE_HASH = "wcf21_cookieHash";

	public static final Pattern PATTERN_PAGE_NUMBER = Pattern.compile(Pattern.quote("http://dotasource.de/notification-list/?pageNo=") + "([0-9]++)",Pattern.DOTALL);
	public static final Pattern PATTERN_POST_CONTENT = Pattern.compile("id=\"text\".*?>(.*?)</textarea>",Pattern.DOTALL);
	public static final Pattern PATTERN_EDIT_POST_FORM = Pattern.compile(Pattern.quote("action=\"http://dotasource.de/post-edit") + ".*?>(.*?)</form>", Pattern.DOTALL);
	public static final Pattern PATTERN_INPUT_TYPE = Pattern.compile("<input type=.*?name=\"(?<name>.*?)\".*?value=\"(?<value>.*?)\"",Pattern.DOTALL);

	private String userId = "56063";
	private String passwordHash = "%242a%2408%24Q6QgPka8tAlGDWAeMbB1Mud3BlXDTm%2FX4R4qVWHlqKMkS0VP2RxFe";
	private String cookieHash = "9125b53871d6b3b342c1ba06ed7c0b04cde3e347";
	private CloseableHttpClient client;

	public DotasourceManager() {
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000).build();

		CookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie(COOKIE_USER_ID, userId);
		cookie.setDomain(".dotasource.de");
		cookie.setPath("/");
		 cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie(COOKIE_PASSWORD, passwordHash);
		cookie.setDomain(".dotasource.de");
		cookie.setPath("/");
		 cookieStore.addCookie(cookie);
		cookie = new BasicClientCookie(COOKIE_HASH, cookieHash);
		cookie.setDomain(".dotasource.de");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		client = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
	}

	public void setSession(String userId, String passwordHash) {
		this.userId = userId;
		this.passwordHash = passwordHash;
	}

	public List<Notification> getNotificationsAfterDate(Date date) throws IOException {
		int currentPage = 1;
		int pageCount = getNotificationPageCount();
		List<Notification> newNotifications = new ArrayList<Notification>();
		while (currentPage <= pageCount) {
			String input = readNotifications(currentPage);
			List<Notification> notifications = Notification.parse(input);
			for (Notification notification : notifications) {
				if (!notification.getDate().before(date)) {
					newNotifications.add(notification);
				} else {
					return newNotifications;
				}
			}
			currentPage++;
		}
		return newNotifications;
	}

	public String getEditPostInput(String postId) throws IOException {
		HttpGet httpGet = new HttpGet("http://dotasource.de/post-edit/" + postId + "/");
		CloseableHttpResponse response = client.execute(httpGet);
		try {
			// System.out.println(response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.forName("UTF-8")))) {
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				return sb.toString();
			}
		} finally {
			if (response != null)
				response.close();
		}
	}

	public String getPost(String postId) throws IOException {
		String input = getEditPostInput(postId);
		Matcher m = PATTERN_POST_CONTENT.matcher(input);
		if (m.find()) {
			return m.group(1);
		} else {
			return null;
		}
	}

	public void updateLikes(Notification notification) throws IOException {
		assert notification.getType() == Notification.TYPE_LIKE;

		ArrayList<Like> likes = new ArrayList<>();
		for (User user : notification.getUsers()) {
			likes.add(new Like(user, notification.getDate(), notification.getPostId()));
		}

		LikeTable likeTable;
		String content;
		String input = getPost(notification.getPostId());
		Matcher m = LikeTable.PATTERN_LIKE_TABLE.matcher(input);
		if (m.find()) { // liketable bereits vorhanden
			content = m.group("content");
			likeTable = LikeTable.parse(m.group("liketable"), notification.getPostId());
			likeTable.addLikes(likes);
		} else {
			content = input;
			likeTable = new LikeTable(likes);
		}
//		System.out.println(content);
		editPost(notification.getPostId(), content + likeTable.toBBCode());
	}

	public void editPost(String postId, String content) throws IOException {

		HashMap<String, String> inputTypes = getInputTypes(postId);

		HttpPost post = new HttpPost("http://dotasource.de/post-edit/" + postId + "/");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("deleteReason", ""));
			nameValuePairs.add(new BasicNameValuePair("subject", inputTypes.get("subject")));
			nameValuePairs.add(new BasicNameValuePair("editReason", "Likes aktualisieren"));
			nameValuePairs.add(new BasicNameValuePair("text", content));
			nameValuePairs.add(new BasicNameValuePair("tmpHash", inputTypes.get("tmpHash")));
			nameValuePairs.add(new BasicNameValuePair("preParse", "1"));
			nameValuePairs.add(new BasicNameValuePair("enableSmilies", "1"));
			nameValuePairs.add(new BasicNameValuePair("enableBBCodes", "1"));
			nameValuePairs.add(new BasicNameValuePair("showSignature", "1"));
			nameValuePairs.add(new BasicNameValuePair("pollQuestion", inputTypes.get("pollQuestion")));
			nameValuePairs.add(new BasicNameValuePair("pollEndTime", inputTypes.get("pollEndTime")));
			nameValuePairs.add(new BasicNameValuePair("pollMaxVotes", "1"));
			nameValuePairs.add(new BasicNameValuePair("t", inputTypes.get("t")));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, Charset.forName("UTF-8")));

			try (CloseableHttpResponse response = client.execute(post)) {
				System.out.println("HTTP Post: http://dotasource.de/post-edit/" + postId + "/");
				 System.out.println(response.getStatusLine());
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					// System.out.println(line);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> getInputTypes(String postId) throws IOException {
		String content = getEditPostInput(postId);
		// System.out.println(content);
		HashMap<String, String> map = new HashMap<>();
		Matcher formMatcher = PATTERN_EDIT_POST_FORM.matcher(content);
		if (formMatcher.find()) {
			// System.out.println("FORM:" + formMatcher.group(1));
			Matcher inputTypeMatcher = PATTERN_INPUT_TYPE.matcher(formMatcher.group(1));
			while (inputTypeMatcher.find()) {
				map.put(inputTypeMatcher.group("name"), inputTypeMatcher.group("value"));
			}
		}
		return map;
	}

	public int getNotificationPageCount() throws IOException {
		String content = readNotifications(1);
		Matcher m = PATTERN_PAGE_NUMBER.matcher(content);
		int result = 0;
		while (m.find()) {
			result = Math.max(result, Integer.parseInt(m.group(1)));
		}
		return result;
	}

	public String readNotifications(int page) throws IOException {
		HttpGet httpGet = new HttpGet("http://www.dotasource.de/notification-list/?pageNo=" + page);
		CloseableHttpResponse response = client.execute(httpGet);
		try {
			System.out.println("HTTP Get: http://www.dotasource.de/notification-list/?pageNo=" + page);
			System.out.println(response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.forName("UTF-8")))) {
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				return sb.toString();
			}
		} finally {
			if (response != null)
				response.close();
		}
	}
}
