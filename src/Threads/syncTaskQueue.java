package Threads;


import java.util.LinkedList;
import java.util.List;

public class syncTaskQueue<E> {

	private List<E> queue;
	private int maxSize; // maximum number of elements queue can hold at a time.
	private int numberOfWaitingThreads;
	private boolean shutDownInitiated;
	public syncTaskQueue(int maxSize) {
		this.maxSize = maxSize;
		queue = new LinkedList<E>();
	}

	/**
	 * Inserts the element into this queue only if space is available
	 * otherwise waits for space to become available.
	 */
	public synchronized void insert(E item) throws InterruptedException {

		// check space is available or not.
		while (queue.size() == maxSize) {
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

		numberOfWaitingThreads++;
		// waits element is available or not.
		while (queue.size() == 0 && !shutDownInitiated) {
			this.wait();
		}
		numberOfWaitingThreads--;
		if (!shutDownInitiated) {
			// element is available, remove element and notify all waiting threads.
			E result = queue.remove(0);
			this.notifyAll();
			return result;
		} else {
			return null;
		}

	}

	/**
	 * Returns current size.
	 */
	public synchronized int size() {
		return queue.size();
	}
	
	/**
	 * Returns current number of waiting threads.
	 */
	public synchronized int getNumberOfWaitingThreads() {
		return numberOfWaitingThreads;
	}
	
	/**
	 * Returns current number of waiting threads.
	 */
	public synchronized void initiateShutDown() {
		shutDownInitiated = true;
		this.notifyAll();
	}
	
}
