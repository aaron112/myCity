package edu.ucsd.mycity.chat;

import java.util.List;

import edu.ucsd.mycity.R;
import edu.ucsd.mycity.chat.ChatRoom.ChatMessage;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


// ******* Used to implement custom listview
public class ChatMsgArrayAdapter extends ArrayAdapter<ChatMessage> {
	private final int textViewResourceId;
	private boolean isMultiUser;

	public ChatMsgArrayAdapter(Activity context, int resource, int textViewResourceId, List<ChatMessage> objects, boolean isMultiUser) {
		super(context, resource, textViewResourceId, objects);
		this.isMultiUser = isMultiUser;
		this.textViewResourceId = textViewResourceId;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = super.getView(position, convertView, parent);
        
		ChatMessage chatmsg = (ChatMessage) super.getItem(position);
        ImageView arrowImageView = (ImageView) rowView.findViewById(R.id.chat_direction);
        TextView textView = (TextView) rowView.findViewById(textViewResourceId);
        
        String dispmsg = "";
        
        if ( chatmsg.getFrom() == null ) {
        	// My Message:
            textView.setGravity(Gravity.RIGHT);
            arrowImageView.setImageResource(R.drawable.arrow_left);
        } else {
            textView.setGravity(Gravity.LEFT);
            arrowImageView.setImageResource(R.drawable.arrow_right);
            if (isMultiUser)
            	dispmsg += chatmsg.getFrom().getName()+":\n";
        }
        
        textView.setText(dispmsg+chatmsg.getMsg());
		
		return rowView;
    }
}
