package vopen.image;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

public class CacheManager <K, V>{
	private Map<K, Entry<K, V>> mCache = new LinkedHashMap<K, Entry<K, V>>();
	private ReferenceQueue<V> mQueue = new ReferenceQueue<V>();//垃圾回收队列
	 
	private static class Entry<K, V> extends SoftReference<V> {
		K mKey;
		
		public Entry(K key, V value, ReferenceQueue<V> queue) {
			super(value, queue);
			mKey = key;
		}
	}
	
	private void cleanUpWeakMap() {
        Entry<K, V> entry = (Entry<K, V>) mQueue.poll();
        while (entry != null) {
        	mCache.remove(entry.mKey);
            entry = (Entry<K, V>) mQueue.poll();
        }
    }

    public synchronized boolean containsKey(K key) {
        cleanUpWeakMap();
        return mCache.containsKey(key);
    }

    public synchronized V put(K key, V value) {
        cleanUpWeakMap();
        Entry<K, V> entry = mCache.put(
                key, new Entry<K, V>(key, value, mQueue));
        return entry == null ? null : entry.get();
    }

    public synchronized V get(K key) {
        cleanUpWeakMap();
        Entry<K, V> entry = mCache.get(key);
        return entry == null ? null : entry.get();
    }

    public synchronized void clear() {
        mCache.clear();
        mQueue = new ReferenceQueue<V>();
    }
    
    public synchronized int size(){
    	return mCache.size();
    }
}
