package dotasource.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Notification {

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_LIKE = 1;
	public static final int TYPE_QUOTE = 2;
	public static final Pattern NOTIFICATION_PATTERN = Pattern.compile("\"jsNotificationItem notificationItem.*?</li>\\s*(<li class=|</ul>)");
	private static final Pattern USER_PATTERN_INFO_BLOCKS = Pattern.compile("<a.*?class=\"userLink\" data-user-id=\"(?<id>.*?)\">(?<name>.*?)</a>");
	private static final Pattern USER_PATTERN = Pattern.compile("title=\"(?<name>.*?)\">\\s*<a href=\"http://dotasource.de/user/(?<id>[0-9]+)");
	private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("<time .*?data-timestamp=\"([0-9]*?)\"");
//	private static final Pattern POST_ID_PATTERN = Pattern.compile("data-link=\"http://dotasource.de/thread/.*?#post([0-9]+)\"");
	private static final Pattern POST_ID_PATTERN = Pattern.compile("Beitrag im Thema <a href=\"http://dotasource.de/thread/[0-9]+.*?postID=([0-9]+)");
	

	private ArrayList<User> users;
	private String postId;
	private int type;
	private Date date;

	public Notification(String postId, int type, Date date, Collection<User> users) {
		this.postId = postId;
		this.type = type;
		this.date = date;
		this.users = new ArrayList<>(users);
	}

	public List<User> getUsers() {
		return users;
	}

	public String getPostId() {
		return postId;
	}

	public int getType() {
		return type;
	}

	public Date getDate() {
		return date;
	}

	public static List<Notification> parse(String htmlInput) {
		htmlInput = htmlInput.replace("\r", "");
		htmlInput = htmlInput.replace("\n", "");
		// htmlInput = htmlInput.replaceAll(" href=\".*?\"", "");
		// htmlInput = htmlInput.replaceAll("<img.*?/>", "");
		// htmlInput = htmlInput.replaceAll("<div.*?>", "");
		// htmlInput = htmlInput.replaceAll("</div>", "");
		// htmlInput = htmlInput.replaceAll("<span.*?>", "");
		// htmlInput = htmlInput.replaceAll("</span>", "");
		// htmlInput = htmlInput.replaceAll("\\t+", "\t");

		ArrayList<Notification> notifications = new ArrayList<>();

		Matcher notificationMatcher = NOTIFICATION_PATTERN.matcher(htmlInput);

		System.out.println(htmlInput);
		while (notificationMatcher.find()) {
			String notificationInput = notificationMatcher.group(0);
			System.out.println(notificationInput);

			// Post-ID
			Matcher postIdMatcher = POST_ID_PATTERN.matcher(notificationInput);
			String postId;
			if (postIdMatcher.find()) {
				postId = postIdMatcher.group(1);
			} else {
				continue;
			}
			// System.out.println("Post-ID: " + postId);

			// Date
			Date date;
			Matcher timestampMatcher = TIMESTAMP_PATTERN.matcher(notificationInput);
			if (timestampMatcher.find()) {
				String timestamp = timestampMatcher.group(1);
				date = new Date(Long.valueOf(timestamp) * 1000);
			} else {
				continue;
			}

			// Type
			int type = TYPE_UNKNOWN;
			if (notificationInput.contains(" gefällt Ihr Beitrag im Thema <a")) {
				type = TYPE_LIKE;
			} else if (notificationInput.contains("</a> zitiert.")) {
				type = TYPE_QUOTE;
			}
			// System.out.print("Typ: ");
			// if (type == TYPE_LIKE)
			// System.out.println("Like");
			// if (type == TYPE_QUOTE)
			// System.out.println("Zitat");
			// if (type == TYPE_UNKNOWN)
			// System.out.println("unbekannt");

			// Users
			HashSet<User> users = new HashSet<>();
			Matcher userMatcher = USER_PATTERN.matcher(notificationInput);
			while (userMatcher.find()) {
				String id = userMatcher.group("id");
				String name = userMatcher.group("name");
				User user = new User(id, name);
				users.add(user);
				// System.out.print(id + " " + name + "; ");
			}

			userMatcher = USER_PATTERN_INFO_BLOCKS.matcher(notificationInput);
			while (userMatcher.find()) {
				String id = userMatcher.group("id");
				String name = userMatcher.group("name");
				User user = new User(id, name);
				users.add(user);
				// System.out.print(id + " " + name + "; ");
			}

			notifications.add(new Notification(postId, type, date, users));
			System.out.println(new Notification(postId, type, date, users));
			// System.out.println();
			// System.out.println(notificationInput);
			// System.out.println();
		}
		return notifications;
	}

	@Override
	public String toString() {
		String string = "Post-ID: " + postId;
		string += "\n";
		string += "Typ: ";
		if (type == TYPE_LIKE)
			string += "Like";
		else if (type == TYPE_QUOTE)
			string += "Zitat";
		else
			string += "unbekannt";
		string += "\n";
		string += LikeTable.DATE_FORMAT.format(date);
		string += "\n";
		for (User user : users) {
			string += user.getName() + " (" + user.getId() + ");";
		}
		return string;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Notification) {
			return date.equals(((Notification) other).date) && postId.equals(((Notification) other).postId) && users.containsAll(((Notification) other).getUsers())
					&& ((Notification) other).getUsers().containsAll(users);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (date + "#" + postId).hashCode();
	}
}
