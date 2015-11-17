package dotasource.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import dotasource.model.Like;
import dotasource.model.LikeTable;
import dotasource.model.Notification;
import dotasource.model.User;

public class Test {

	public static void main(String[] args) throws IOException, ParseException {
		ArrayList<User> users = new ArrayList<>();
		users.add(new User("1337", "yolobernd"));
		Notification notification = new Notification("1337", Notification.TYPE_LIKE, new Date(0), users);
		updateLikes(notification);
	}

	public static void testLikeTable() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("src/dotasource/api/input.txt"));
		String line;
		String input = "";
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		br.close();
		DotasourceManager manager = new DotasourceManager();
		ArrayList<User> users = new ArrayList<>();
		users.add(new User("1337", "yolobernd"));
		LikeTable likeTable = LikeTable.parse(input, "1337");
		System.out.println(likeTable);
	}

	public void testNotifications() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("src/dotasource/api/notifications.html"));
		String input = "";
		String line;
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		br.close();
		List<Notification> notifications = Notification.parse(input);
		for (Notification notification : notifications) {
			System.out.println(notification);
			System.out.println();
		}
	}
	
	public static void updateLikes(Notification notification) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader("src/dotasource/api/input.txt"));
		String input = "";
		String line;
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		br.close();
		
		assert notification.getType() == Notification.TYPE_LIKE;

		ArrayList<Like> likes = new ArrayList<>();
		for (User user : notification.getUsers()) {
			likes.add(new Like(user, notification.getDate(), notification.getPostId()));
		}

		LikeTable likeTable;
		String content;
		Matcher m = LikeTable.PATTERN_LIKE_TABLE.matcher(input);
		if (m.find()) { // liketable bereits vorhanden
			content = m.group("content");
			likeTable = LikeTable.parse(m.group("liketable"), notification.getPostId());
			likeTable.addLikes(likes);
		} else {
			content = input;
			likeTable = new LikeTable(likes);
		}
		System.out.println(content + likeTable.toBBCode());
	}
}
