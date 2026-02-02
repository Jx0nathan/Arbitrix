package io.arbitrix.affinity;

import net.openhft.affinity.AffinityLock;

import static net.openhft.affinity.AffinityStrategies.*;

public class AffinityLockBindMain {

    public static void main(String[] args) throws InterruptedException {
        AffinityLock al = AffinityLock.acquireLock();
        try {
            // find a cpu on a different socket, otherwise a different core.
            // bind = false -> 预留一个CPU，但不将当前线程绑定到该CPU上(CPU被保留，以便稍后可以将线程绑定到该CPU上)
            // DIFFERENT_SOCKET / DIFFERENT_CORE -> 选择核的策略
            AffinityLock readerLock = al.acquireLock(DIFFERENT_SOCKET, DIFFERENT_CORE);
            new Thread(new SleepRunnable(readerLock, false), "reader").start();

            // find a cpu on the same core, or the same socket, or any free cpu.
            AffinityLock writerLock = readerLock.acquireLock(SAME_CORE, SAME_SOCKET, ANY);
            new Thread(new SleepRunnable(writerLock, false), "writer").start();

            Thread.sleep(200);
        } finally {
            al.release();
        }
    }

    static class SleepRunnable implements Runnable {
        private final AffinityLock affinityLock;
        private final boolean wholeCore;

        SleepRunnable(AffinityLock affinityLock, boolean wholeCore) {
            this.affinityLock = affinityLock;
            this.wholeCore = wholeCore;
        }

        public void run() {
            affinityLock.bind(wholeCore);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                affinityLock.release();
            }
        }
    }
}
