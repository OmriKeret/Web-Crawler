package Threads;

import java.util.LinkedList;
import java.util.List;

public class syncTaskQueue<E> {

	private List<E> queue;
	private int maxSize; // maximum number of elements queue can hold at a time.

	public syncTaskQueue(int maxSize) {
		this.maxSize = maxSize;
		queue = new LinkedList<E>();
	}

	/**
	 * Inserts the element into this queue only if space is available
	 * else waits for space to become available.
	 */
	public synchronized void insert(E item) throws InterruptedException {

		// check space is available or not.
		if (queue.size() == maxSize) {
			this.wait();
		}

		// space is available, insert element and notify all waiting threads.
		queue.add(item);
		this.notifyAll();
	}

	/**
	 * Retrieves and removes the head of this queue if elements are
	 * available otherwise waits for element to become available.
	 */
	public synchronized E remove() throws InterruptedException {

		// waits element is available or not.
		while (queue.size() == 0) {
			this.wait();
		}

		// element is available, remove element and notify all waiting threads.
		E result = queue.remove(0);
		this.notifyAll();
		return result;

	}

	/**
	 * Returns size of syncTaskQueue.
	 */
	public synchronized int size() {
		return queue.size();
	}

}
