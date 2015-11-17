package dotasource.model;

import java.util.Date;

public class Like implements Comparable<Like> {

	public static final String LIKE_ICON_URL = "http://abload.de/img/likexjp8k.png";
	
	private User user;
	private Date date;
	private String postId;

	public Like(User user, Date date, String postId) {
		this.user = user;
		this.date = date;
		this.postId = postId;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Like) {
			Like like = (Like) other;
			return user.equals(like.user) && postId.equals(like.postId);
		}
		return false;
	}

	public String toBBCode() {
		String string = "[tr]";
		string += "[td][img]"+LIKE_ICON_URL+"[/img][/td]";
		string += "[td]" + LikeTable.DATE_FORMAT_BB.format(date) + "[/td]";
		string += "[td][url='" + user.getUrl() + "']" + user.getName() + "[/url][/td]";
		string += "[/tr]";
		return string;
	}

	@Override
	public String toString() {
		return LikeTable.DATE_FORMAT.format(date) + ": " + user;
	}

	@Override
	public int compareTo(Like like) {
		return date.compareTo(like.date);
	}
}
