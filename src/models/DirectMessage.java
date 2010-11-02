package models;
import java.util.Vector;


/**
 * �M���u�_�C���N�g���b�Z�[�W�v
 * ���f���ƕ`��S���̃N���X
 **/
public class DirectMessage extends Element {
	//���[�U���B�ȃp�P�p�B
	public static User[] usercache;
	public static int parsing_pos;
	String text;
	User sender, recipient;
	
	/*Out[10]: DirectMessage(
	 * {u'created_at': 1274922146,
	 * u'sender': User(����),
	 * u'text': u'�i����',
	 * u'sender_screen_name': u'jinjitter',
	 * u'sender_id': 115381785,
	 * 'created_at_local': 1274954546,
	 * u'recipient_id': 38650958,
	 * u'recipient_screen_name': u'moondial0',
	 * u'recipient': User(����),
	 * u'id': 1173140166})
	 �K�v�Ȃ̂�ID,���t,��,�����Ǝ󂯎胆�[�U */
	
	// DirectMessage[-id, -created_at, sender, recipient, text]
	
	protected DirectMessage(Object[] _) {
		super(_);
		int i = 0;
		
		id = ((Long)_[i++]).longValue();
		created_at = ((Long)_[i++]).longValue();
		
		// ���[�U���i�d���ȗ��ρj��W�J
		if(_[i] instanceof Long)
			sender = usercache[(int)((Long)_[i]).longValue()];
		else {
			sender = User.parse((Object[])_[i]);
			usercache[parsing_pos] = sender;
		}
		++parsing_pos; ++i;
		
		// ���[�U���i�d���ȗ��ρj��W�J
		if(_[i] instanceof Long)
			recipient = usercache[(int)((Long)_[i]).longValue()];
		else {
			recipient = User.parse((Object[])_[i]);
			usercache[parsing_pos] = recipient;
		}
		++parsing_pos; ++i;
		
		setText((String)_[i]);
	}
	
	public static void unpack(Object[] _, Vector data) {
		usercache = new User[1000];
		parsing_pos = 0;
		long latest_id = 0, latest_date = 0;
		for(int i=0; i<_.length; ++i) {
			DirectMessage s = new DirectMessage((Object[])_[i]);
			latest_id -= s.id;
			s.id = latest_id;
			latest_date -= s.created_at;
			s.created_at = latest_date;
			data.addElement(s);
		}
		usercache = null;
		//Tuwi.log(s[0].text);
	}
	// TODO:�`��, getlinkypos
	public String toString() {
		return created_at+sender.screen_name + "(" + getDate() + "): " + text;
	}
}
