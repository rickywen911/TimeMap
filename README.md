# TimeMap
A light weight time map with build in time check thread.

TimeMap is a light weight timeMap with a build in time check thread. 

  - Add item and check item in the same time(Concurrent and thread safe).
  - Light weight.
  - Auto delete item when item time out.

# How to use
Simply add timemapcore module in your app project.

# Example
First your item must implement ITimeMapItem interface and provide a long variable indicate time.
Like Peer.java
```java
public class Peer implements ITimeMapItem {

	long mTime = 0;
	String id;

	public Peer(long mTime, String id) {
		this.mTime = mTime;
		this.id = id;
	}
	@Override
	public long getTime() {
		return mTime;
	}
	@Override
	public String toString() {
		return id;
	}
}
```
Second Create a TimeMap stores Peer with Builder class. You will need two listeners to create a TimeMap. onNewItemListener and
onItemRemoveListener.
```java
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
```
Use builder to create a time map
```java
mTimeMap = new TimeMap.Builder<Peer>()
				.setNewItemListener(newItemListener)
				.setItemRemoveListener(onItemRemoveListener)
				.build();
```
All set! Just use it!
```java
//now you can make one thread add items and timemap thread check items
//when you get a new item info in other thread just add it to TimeMap
mTimeMap.AddItem("1",new Peer(System.currentTimeMillis(), "testpeer1"));
```
# License
License under [BSD-2-Clause](https://opensource.org/licenses/BSD-2-Clause)
