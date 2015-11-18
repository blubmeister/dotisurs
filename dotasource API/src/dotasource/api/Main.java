package dotasource.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import dotasource.model.Notification;

public class Main {

	private static HashSet<Notification> oldNotifications = new HashSet<>();

	public static void main(String[] args) throws IOException, InterruptedException {
		DotasourceManager manager = new DotasourceManager();
		Date newestdate = new Date(System.currentTimeMillis() - 60 * 1000 * 60);
		Notification latestNotification = null;
		while (true) {
			Thread.sleep(30 * 1000);
			System.out.println("Uhrzeit: " + new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
			System.out.println("Letzte Notification: " + new SimpleDateFormat("HH:mm:ss").format(newestdate));
			List<Notification> notifications = manager.getNotificationsAfterDate(new Date(newestdate.getTime() - 5 * 60 * 1000));// 5 minutes buffer since notifications are random
			System.out.println(notifications.size());
			if (notifications.size() > 0) {
				if (notifications.get(0).equals(latestNotification)) {
					continue;
				}
				latestNotification = notifications.get(0);
			}
			for (Notification notification : notifications) {
				if (oldNotifications.contains(notification)) {
					continue;
				}
				System.out.println(notification);
				Date date = notification.getDate();
				if (date.after(newestdate)) {
					newestdate = date;
				}
				try {
					manager.updateLikes(notification);
					oldNotifications.add(notification);
				} catch (Exception e) {
					System.err.println(e);
				}
				Thread.sleep(1000);
			}
		}
	}
}
