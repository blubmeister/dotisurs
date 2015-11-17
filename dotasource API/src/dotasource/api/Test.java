package dotasource.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dotasource.model.LikeTable;
import dotasource.model.Notification;
import dotasource.model.User;

public class Test {

	public static void main(String[] args) throws IOException, ParseException {
		BufferedReader br = new BufferedReader(new FileReader("src/dotasource/api/input.txt"));
		String line;
		String input = "";
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		br.close();
		LikeTable likeTable = LikeTable.parse(input, "123");
		// System.out.println(likeTable.toBBCode());

		br = new BufferedReader(new FileReader("src/dotasource/api/notifications.html"));
		input = "";
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		br.close();
		List<Notification> notifications = Notification.parse(input);
		for (Notification notification : notifications) {
			System.out.println(notification);
			System.out.println();
		}

		DotasourceManager manager = new DotasourceManager();
		ArrayList<User> users = new ArrayList<>();
		users.add(new User("1337", "yolobernd"));
		Notification notification = new Notification("1253273", Notification.TYPE_LIKE, new Date(1447778808000L), users);
		manager.updateLikes(notification);
	}
}
