package rickywen.com.timemap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import rickywen.com.timemapcore.TimeMap;

public class MainActivity extends AppCompatActivity {

	private TimeMap<Peer> mTimeMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TimeMap.onNewItemListener<Peer> newItemListener = new TimeMap.onNewItemListener<Peer>() {
			@Override
			public void onNewItem(String key, Peer peer) {
				Toast.makeText(getApplicationContext(), "newItem"+peer.toString(), Toast.LENGTH_LONG).show();
			}
		};

		TimeMap.onItemRemoveListener<Peer> onItemRemoveListener = new TimeMap.onItemRemoveListener<Peer>() {
			@Override
			public void onItemRemove(String key, Peer peer) {
				Toast.makeText(getApplicationContext(), "leaveItem"+peer.toString(), Toast.LENGTH_LONG).show();
			}
		};

		//example of create a timemap thread
		mTimeMap = new TimeMap.Builder<Peer>()
						.setNewItemListener(newItemListener)
						.setItemRemoveListener(onItemRemoveListener)
						.build();

		//now you can make one thread add items and timemap thread check items
		//when you get a new item info in other thread just add it to TimeMap
		mTimeMap.AddItem("1",new Peer(System.currentTimeMillis(), "testpeer1"));

	}
}
