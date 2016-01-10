package Threads;

public class customThreadPool {

	private syncTaskQueue<Runnable> taskQueue;

	/*
	 * Once pool shutDown will be initiated, poolShutDownStarted will become
	 * true.
	 */
	private boolean poolShutDownStarted = false;

	/*
	 * Constructor of ThreadPool nThreads= is a number of threads that exist in
	 * ThreadPool. nThreads number of threads are created and started. *
	 */
	public customThreadPool(int numberOfThreads) {
		taskQueue = new syncTaskQueue<Runnable>(numberOfThreads);

		// Create and start nThreads number of threads.
		for (int i = 1; i <= numberOfThreads; i++) {
			TaskThread threadPoolsThread = new TaskThread(taskQueue, this);
			threadPoolsThread.setName("Thread-" + i);
			System.out.println("Thread-" + i + " created in ThreadPool.");
			threadPoolsThread.start(); // start thread
		}

	}

	/**
	 * Execute the task, task must be of Runnable type.
	 */
	public synchronized void execute(Runnable task) throws Exception {
		if (this.poolShutDownStarted)
			throw new Exception("ThreadPool has been shutDown, no further tasks can be added");

		/*
		 * Add task in sharedQueue, and notify all waiting threads that task is
		 * available.
		 */
		System.out.println("task has been added.");
		this.taskQueue.insert(task);
	}

	public boolean isPoolShutDownInitiated() {
		return poolShutDownStarted;
	}

	/**
	 * Initiates shutdown of ThreadPool, previously submitted tasks are
	 * executed, but no new tasks will be accepted.
	 */
	public synchronized void shutdown() {
		this.poolShutDownStarted = true;
		System.out.println("ThreadPool SHUTDOWN initiated.");
	}

}