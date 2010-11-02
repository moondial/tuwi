package models;

public class UserDetail extends User {
	
	boolean following, verified, isprotected, geo_enabled, contributors_enabled;
	int offset, tweets, friends, followers, favorites;
	String timezone, lang, url, bio;
	
	protected UserDetail(Object[] _) {
		super(_);
		int i = 0;
		long flg = ((Long)_[i++]).longValue();
		boolean full = (flg & 1) == 1;
		screen_name = (String)_[i++];
		if(full || ((flg & 2) != 0))
			id = ((Long)_[i++]).longValue();
		if(full || ((flg & 4) != 0))
			name = (String)_[i++];
		if(full || ((flg & 8) != 0))
			imgid = ((Long)_[i++]).longValue();
		if(full || ((flg & 16) != 0))
			location = (String)_[i++];
		if(full) {
			offset = (int)((Long)_[i++]).longValue();
			timezone = (String)_[i++];
			lang = (String)_[i++];
			url = (String)_[i++];
			bio = (String)_[i++];
			tweets = (int)((Long)_[i++]).longValue();
			friends = (int)((Long)_[i++]).longValue();
			followers = (int)((Long)_[i++]).longValue();
			favorites = (int)((Long)_[i++]).longValue();
			created_at = ((Long)_[i++]).longValue();
			flg = ((Long)_[i]).longValue();
			following = (flg & 1) != 0;
			isprotected = (flg & 2) != 0;
			verified = (flg & 4) != 0;
			geo_enabled = (flg & 8) != 0;
			contributors_enabled = (flg & 16) != 0;
		}
	}
}
