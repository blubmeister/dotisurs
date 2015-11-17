package dotasource.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import dotasource.model.Like;
import dotasource.model.LikeTable;
import dotasource.model.Notification;
import dotasource.model.User;

public class DotasourceManager {

	private String sessionId;

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<Notification> getNotificationsAfterDate(Date date) {
		int currentPage = 1;
		int pageCount = getNotificationPageCount();
		List<Notification> newNotifications = new ArrayList<Notification>();
		while (currentPage <= pageCount) {
			String input = readNotifications(currentPage);
			List<Notification> notifications = Notification.parse(input);
			for (Notification notification : notifications) {
				if (notification.getDate().after(date)) {
					newNotifications.add(notification);
				} else {
					return newNotifications;
				}
			}
		}
		return newNotifications;
	}

	public String getPost(String postId) {
		BufferedReader br;
		String input = "";
		try {
			br = new BufferedReader(new FileReader("src/dotasource/api/input.txt"));
		
		String line;
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		br.close();	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return input;
	}

	public void updateLikes(Notification notification) {
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
		editPost(notification.getPostId(), content + likeTable.toBBCode());
	}

	public void editPost(String postId, String content) {
		System.out.println("Neuer post: \n" + content);
	}

	public int getNotificationPageCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String readNotifications(int page) {
		return "";
	}
}
