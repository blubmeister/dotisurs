package dotasource.model;

import java.util.Date;

public class Like implements Comparable<Like> {

	private User user;
	private Date date;

	public Like(User user, Date date) {
		this.user = user;
		this.date = date;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Like) {
			return user.equals(((Like) other).user);
		}
		return false;
	}

	public String toBBCode() {
		String string = "[tr]";
		string += "[td][img]http://abload.de/img/likexjp8k.png[/img][/td]";
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
		return user.compareTo(like.user);
	}
}
