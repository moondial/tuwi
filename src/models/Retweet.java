package models;

public class Retweet extends Status {
	public Status RTed_status;
	public Retweet(Object[] _) {
		super(_);
		RTed_status = new Status((Object[])_[5]);
		text = RTed_status.text;//"RT @" + RTed_status.user.screen_name + ": " + RTed_status.text;  // for unofficial RT
		format = RTed_status.format;
	}
	
	public String toString() {
		return user.screen_name + "(" + getAbsDate() + "): RT @" + RTed_status.user.screen_name + RTed_status.text;
	}
}
