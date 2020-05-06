package me.fiftyfour.punisher.bungee.managers;

import me.fiftyfour.punisher.bungee.PunisherPlugin;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;

public class WorkerManager {

    private static final WorkerManager INSTANCE = new WorkerManager();
    private ArrayList<Worker> workers = new ArrayList<>();
    private boolean locked = true;
    private Thread mainThread;
    private PunisherPlugin plugin = PunisherPlugin.getInstance();

    private WorkerManager() {
    }

    public static WorkerManager getInstance() {
        return INSTANCE;
    }

    public void start() {
        if (plugin == null)
            plugin = PunisherPlugin.getInstance();
        try {
            if (!locked)
                throw new Exception("Worker Manager Already started!");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        mainThread = Thread.currentThread();
        locked = false;
        plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Started Worker Manager!");
    }

    public boolean isStarted() {
        return !locked;
    }

    public synchronized void stop() {
        try {
            if (locked)
                throw new Exception("Worker Manager Already stopped!");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        locked = true;
        try {
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Pausing main thread while workers finish up!");
            if (!workers.isEmpty())
                mainThread.wait();
        } catch (InterruptedException e) {
            plugin.getLogger().severe(plugin.prefix + ChatColor.RED + "Error: main thread was interrupted while waiting for workers to finish!");
            plugin.getLogger().severe(plugin.prefix + ChatColor.RED + "Interrupting workers, this may cause data loss!!");
            PunisherPlugin.LOGS.severe("Error: main thread was interrupted while waiting for workers to finish!");
            PunisherPlugin.LOGS.severe("Interrupting workers, this may cause data loss!!");
            for (Worker worker : workers) {
                plugin.getLogger().severe(plugin.prefix + ChatColor.RED + "Interrupting " + worker.getName());
                PunisherPlugin.LOGS.severe("Interrupting " + worker.getName());
                worker.interrupt();
            }
        }
        workers.clear();
    }

    public synchronized void runWorker(Worker worker) {
        try {
            if (locked)
                throw new Exception("Error: Worker Manager has not yet started!");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        worker.setName("The-Punisher - Worker Thread #" + (workers.isEmpty() ? 1 : workers.size() + 1));
        workers.add(worker);
        worker.start();
    }

    private synchronized void finishedWorker(Worker worker) {
        if (worker.getStatus() == Worker.Status.FINISHED)
            workers.remove(worker);
        if (locked)
            mainThread.notifyAll();
    }


    public static class Worker extends Thread {

        private Runnable runnable;
        private Status status;

        public Worker(Runnable runnable) {
            this.runnable = runnable;
            this.status = Status.CREATED;
        }

        @Override
        public void run() {
            status = Status.WORKING;
            runnable.run();
            status = Status.FINISHED;
            WorkerManager.getInstance().finishedWorker(this);
        }

        public WorkerManager.Worker.Status getStatus() {
            return this.status;
        }


        public enum Status {
            CREATED, WORKING, FINISHED
        }
    }

}
