package dotasource.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import dotasource.model.Notification;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		DotasourceManager manager = new DotasourceManager();
		Date newestdate = new Date(System.currentTimeMillis() - 1000 * 60 * 5);
		Notification latestNotification= null;
		while (true) {
			Thread.sleep(30 * 1000);
			System.out.println("Uhrzeit: " + new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
			System.out.println("Letzte Notification: " + new SimpleDateFormat("HH:mm:ss").format(newestdate));
			List<Notification> notifications = manager.getNotificationsAfterDate(newestdate);
			if (notifications.size() > 0) {
				if(notifications.get(0).equals(latestNotification)){
					continue;
				}
				latestNotification = notifications.get(0);
			}
			for (Notification notification : notifications) {
				if (notification.getType() != Notification.TYPE_LIKE)
					continue;
				System.out.println(notification);
				Date date = notification.getDate();
				if (date.after(newestdate)) {
					newestdate = date;
				}
				try {
					manager.updateLikes(notification);
				} catch (Exception e) {
					System.out.println(e);
				}
				Thread.sleep(1000);
			}
		}
	}
}
