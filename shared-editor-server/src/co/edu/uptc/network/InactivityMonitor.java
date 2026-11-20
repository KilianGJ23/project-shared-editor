package co.edu.uptc.network;

public class InactivityMonitor implements Runnable {
    private long checkIntervalMillis;
    private long maxInactivityMillis;
    private boolean running;
    private ServerMessageListener listener;

    public InactivityMonitor(long checkIntervalMillis, long maxInactivityMillis, ServerMessageListener listener) {
        this.checkIntervalMillis = checkIntervalMillis;
        this.maxInactivityMillis = maxInactivityMillis;
        this.listener = listener;
        this.running = true;
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                Thread.sleep(this.checkIntervalMillis);
                if (this.running && this.listener != null) {
                    this.listener.onInactivityCheck(this.maxInactivityMillis);
                }
            } catch (InterruptedException ex) {
                this.running = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        this.running = false;
    }

    public long getMaxInactivityMillis() {
        return this.maxInactivityMillis;
    }
}
