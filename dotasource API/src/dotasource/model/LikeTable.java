package dotasource.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikeTable {

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy', 'HH:mm");
	public static final DateFormat DATE_FORMAT_BB = new SimpleDateFormat("'[i]'dd.MM.yyyy'[/i], 'HH:mm");
	private static final Pattern PATTERN_TR = Pattern.compile(Pattern.quote("[tr]") + "(.*?)" + Pattern.quote("[/tr]"));
	private static final Pattern PATTERN_TD = Pattern.compile(Pattern.quote("[td]") + "(.*?)" + Pattern.quote("[/td]"));
	private static final Pattern PATTERN_USER = Pattern.compile(Pattern.quote("[url='http://dotasource.de/user/") + "(?<id>[0-9]+)-.+'\\](?<name>.+)" + Pattern.quote("[/url]"));

	private HashSet<Like> likes = new HashSet<>();

	public static LikeTable parse(String input, String postId) throws ParseException {
		input = input.replace("\r", "");
		input = input.replace("\n", "");

		Matcher matcherTr = PATTERN_TR.matcher(input);
		LikeTable likeTable = new LikeTable();
		while (matcherTr.find()) {
			String likeinput = matcherTr.group(1);
			Matcher matcherTd = PATTERN_TD.matcher(likeinput);

			// Icon
			matcherTd.find();

			// Date
			matcherTd.find();
			String dateinput = matcherTd.group(1);
			dateinput = dateinput.replace("[td]", "");
			dateinput = dateinput.replace("[/td]", "");
			dateinput = dateinput.replace("[i]", "");
			dateinput = dateinput.replace("[/i]", "");
			Date date = DATE_FORMAT.parse(dateinput);

			// User
			matcherTd.find();
			String userinput = matcherTd.group(1);
			Matcher matcherUser = PATTERN_USER.matcher(userinput);
			matcherUser.find();
			String id = matcherUser.group("id");
			String name = matcherUser.group("name");
			User user = new User(id, name);

			likeTable.likes.add(new Like(user, date, postId));
		}
		return likeTable;
	}

	public String toBBCode() {

		ArrayList<Like> likelist = new ArrayList<>(likes);
		Collections.sort(likelist);
		String string = "[align=right]\n";
		string += "[spoiler=";
		string += likes.size();
		string += " Like";
		if (likes.size() != 1) {
			string += "s";
		}
		string += "]\n";
		string += "[table]\n";
		for (Like like : likelist) {
			string += like.toBBCode() + "\n";
		}
		string += "[/table]\n";
		string += "[/spoiler]\n";
		string += "[/align]";
		return string;
	}

	@Override
	public String toString() {
		String string = "";
		for (Like like : likes) {
			string += like + "\n";
		}
		return string;
	}
}
