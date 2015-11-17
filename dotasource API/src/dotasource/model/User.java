package dotasource.model;

public class User implements Comparable<User> {
	private final String id;
	private String name;

	public User(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return "http://dotasource.de/user/" + id;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof User) {
			return id.equals(((User) other).id);
		}
		return false;
	}

	@Override
	public String toString() {
		return name + " (" + id + ")";
	}

	public int compareTo(User user) {
		return id.compareTo(user.id);
	}
}
