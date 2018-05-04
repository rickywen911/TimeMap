/**
 * Copyright (c) 2018, rickywen
 * Email: chaoxiangwen@gmail.com
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies,
 either expressed or implied, of the TimeMap project.
 */

package rickywen.com.timemapcore;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class TimeMap<T extends ITimeMapItem> implements Callable {

	/**
	 * New Item listener.
	 * @param <T>
	 */
	public interface onNewItemListener<T> {

		/**
		 * Called when there is a new item add to time map.
		 * @param key
		 * @param t new item.
		 */
		void onNewItem(String key ,T t);

	}

	/**
	 * Update item listener.
	 * @param <T>
	 */
	public interface onItemUpdateListener<T> {

		/**
		 * Called when a item already in time map update.
		 * @param key
		 * @param t updated item.
		 */
		void onItemUpdate(String key ,T t);

	}

	/**
	 * Remove item listener.
	 * @param <T>
	 */
	public interface onItemRemoveListener<T> {

		/**
		 * Called when a item time out and remove from time map.
		 * @param key
		 * @param t removed item.
		 */
		void onItemRemove(String key ,T t);

	}

	/**
	 * Stop listener.
	 * Called when task stop.
	 */
	public interface onTaskStop {
		void onStop(Set<String> keySet);
	}

	private volatile boolean m_isRunning = false;

	private long m_timeOut;
	private long m_currentTimeMillis;
	private long m_sleep;

	private onItemRemoveListener<T> m_itemRemove;
	private onItemUpdateListener<T> m_itemUpdate;
	private onNewItemListener<T> m_newItem;
	private onTaskStop m_stopListener;

	private ConcurrentHashMap<String,T> m_timeMap = new ConcurrentHashMap<>();


	/**
	 * Constructor
	 * @param timeout
	 * @param sleeptime
	 * @param newItemListener
	 * @param itemUpdateListener
	 * @param itemRemoveListener
	 * @param stopListener
	 */
	private TimeMap(long timeout,
					long sleeptime,
					onNewItemListener<T> newItemListener,
					onItemUpdateListener<T> itemUpdateListener,
					onItemRemoveListener<T> itemRemoveListener,
					onTaskStop stopListener) {
		this.m_timeOut = timeout;
		this.m_sleep = sleeptime;
		this.m_newItem = newItemListener;
		this.m_itemUpdate = itemUpdateListener;
		this.m_itemRemove = itemRemoveListener;
		this.m_stopListener = stopListener;
	}


	/**
	 * Add or update a item in map.
	 * @param key A string, can be IP, UUID, ID etc.
	 * @param t
	 */
	public void AddItem(String key ,T t) {
		if(m_isRunning) {
			if(m_timeMap.contains(key)) {
				m_timeMap.replace(key, t);
				if(m_itemUpdate != null) {
					m_itemUpdate.onItemUpdate(key,t);
				}
			} else {
				m_timeMap.put(key, t);
				m_newItem.onNewItem(key ,t);
			}
		}
	}

	public void AbortTask() {
		m_isRunning = false;
	}

	@Override
	public Object call() throws Exception {
		while(m_isRunning) {
			if(m_timeMap.isEmpty()) {
				Thread.sleep(m_sleep);
				continue;
			}
			for(Iterator<Map.Entry<String, T>> it = m_timeMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, T> entry = it.next();

				String key = entry.getKey();
				T value = entry.getValue();
				m_currentTimeMillis = System.currentTimeMillis();
				if (m_currentTimeMillis - value.getTime() > m_timeOut) {
					it.remove();
					m_itemRemove.onItemRemove(key,value);
				}
			}
		}

		if(m_stopListener != null) {
			if(!m_timeMap.isEmpty()) {
				m_stopListener.onStop(m_timeMap.keySet());
			} else {
				m_stopListener.onStop(null);
			}
		}
		return null;
	}

	/**
	 * Builder for time map.
	 * @param <T>
	 */
	public static class Builder<T extends ITimeMapItem> {

		private long m_timeOut = 10000;
		private long m_sleep = 1000;

		private onItemRemoveListener<T> m_itemRemove;
		private onItemUpdateListener<T> m_itemUpdate;
		private onNewItemListener<T> m_newItem;
		private onTaskStop m_stopListener;

		public Builder() {

		}

		/**
		 * Set timemap item time out.
		 * @param timeOut
		 * @return
		 */
		public Builder<T> setTimeOut(long timeOut) {
			this.m_timeOut = timeOut;

			return this;
		}

		/**
		 * Set timemap check time
		 * @param sleepTime
		 * @return
		 */
		public Builder<T> setSleepTime(long sleepTime) {
			this.m_sleep = sleepTime;

			return this;
		}

		/**
		 * Set timemap item reomve listener.
		 * @param itemRemoveListener
		 * @return
		 */
		public Builder<T> setItemRemoveListener(onItemRemoveListener<T> itemRemoveListener) {
			this.m_itemRemove = itemRemoveListener;

			return this;
		}

		/**
		 * Set timemap new item listener
		 * @param newItemListener
		 * @return
		 */
		public Builder<T> setNewItemListener(onNewItemListener<T> newItemListener) {
			this.m_newItem = newItemListener;

			return this;
		}

		/**
		 * Set timemap thread stop listener.
		 * @Option
		 * @param taskStopListener
		 * @return
		 */
		public Builder<T> setTaskStopListener(onTaskStop taskStopListener) {
			this.m_stopListener = taskStopListener;

			return this;
		}

		/**
		 * Set timemap item update listener.
		 * @Option
		 * @param itemUpdateListener
		 * @return
		 */
		public Builder<T> setItemUpdateListener(onItemUpdateListener<T> itemUpdateListener) {
			this.m_itemUpdate = itemUpdateListener;

			return this;
		}

		/**
		 * Build a timemap.
		 * @return
		 */
		public TimeMap<T> build() {
			return new TimeMap<>(m_timeOut, m_sleep, m_newItem, m_itemUpdate, m_itemRemove, m_stopListener);
		}
	}
}
