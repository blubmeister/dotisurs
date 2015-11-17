package dotasource.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import dotasource.model.LikeTable;

public class Test {

	public static void main(String[] args) throws IOException, ParseException {
		BufferedReader br = new BufferedReader(new FileReader("src/dotasource/api/input.txt"));
		String line;
		String input = "";
		while ((line = br.readLine()) != null) {
			input += line + "\n";
		}
		LikeTable likeTable = LikeTable.parse(input);
		System.out.println(likeTable.toBBCode());
	}
}
