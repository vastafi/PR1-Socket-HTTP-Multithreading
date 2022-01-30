package out;

import java.util.concurrent.Semaphore;

class Philosopher extends Thread {
    Semaphore sem;
    int num = 0;
    int id;

    Philosopher(Semaphore sem, int id) {
        this.sem = sem;
        this.id = id;
    }

    public void run() {
        while(true) {
            try {
                if (this.num < 3) {
                    this.sem.acquire();
                    System.out.println("Step " + this.id + "run");
                    sleep(500L);
                    ++this.num;
                    System.out.println("Step" + this.id + "wrong");
                    this.sem.release();
                    sleep(500L);
                    continue;
                }
            } catch (InterruptedException var2) {
                System.out.println("Step" + this.id + "pause");
            }

            return;
        }
    }
}
