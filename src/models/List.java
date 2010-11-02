package models;

public class List extends Element {
	public static final int PUBLIC = 0;
	public static final int PRIVATE = 1;


	public String full_name, description;
	public int id, subscribers, members, mode/* = constant */;
	public boolean following;
	//public User user;
	
	public List(Object[] _) {
		super(_);
		int i = 0;
		
		id = (int)((Long)_[i++]).longValue();
		//created_at = ((Long)_[i++]).longValue();
		subscribers = (int)((Long)_[i++]).longValue();
		members = (int)((Long)_[i++]).longValue();
		mode = (int)((Long)_[i++]).longValue();
		following = ((Boolean)_[i++]).booleanValue();
		//full_name
		//description
		
		//Tuwi.log(_[i]);
		// ÉÜÅ[ÉUèÓïÒ
		//user = User.parse((Object[])_[i]);
		
	}
}
