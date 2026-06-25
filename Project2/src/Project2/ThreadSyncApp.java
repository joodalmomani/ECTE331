package Project2;



 // Utility class to calculate sums via looping. 
class MathUtility { 

    public static int calculateSum(int n) { 

        int sum = 0; 

        for (int i = 0; i <= n; i++) { 

            sum += i; 

        } 

        return sum; 

    } 

} 


// Shared memory object containing variables and state flags for synchronization. 

class SharedMemory { 

    int A1, A2, A3; 

    int B1, B2, B3; 


    // Synchronization state flags 

    boolean a1Done = false; 

    boolean b2Done = false; 

    boolean a2Done = false; 

    boolean b3Done = false; 

} 


// Thread A implementation 

class ThreadA extends Thread { 

    private final SharedMemory shared; 

    public ThreadA(SharedMemory shared) { 

        this.shared = shared; 

    } 
    

    @Override 
    public void run() { 
        try { 
            // FuncA1 
            shared.A1 = MathUtility.calculateSum(500); 

            synchronized (shared) { 

                shared.a1Done = true; 

                shared.notifyAll(); // Signal B2 that A1 is ready 

            } 

            // FuncA2 
            synchronized (shared) { 

                while (!shared.b2Done) { 

                    shared.wait(); // Passive wait for B2 

                } 

            } 

            shared.A2 = shared.B2 + MathUtility.calculateSum(300); 

            synchronized (shared) { 

                shared.a2Done = true; 

                shared.notifyAll(); // Signal B3 that A2 is ready 

            } 

            // FuncA3 
            synchronized (shared) { 

                while (!shared.b3Done) { 

                    shared.wait(); // Passive wait for B3 

                } 

            } 

            shared.A3 = shared.B3 + MathUtility.calculateSum(400); 

        } catch (InterruptedException e) { 

            Thread.currentThread().interrupt(); 

        } 

    } 

} 

 

//Thread B implementation 

class ThreadB extends Thread { 

    private final SharedMemory shared; 

    public ThreadB(SharedMemory shared) { 

        this.shared = shared; 

    } 
    

    @Override 

    public void run() { 

        try { 

            // FuncB1 (No dependencies, can run immediately) 

            shared.B1 = MathUtility.calculateSum(250); 

            // FuncB2 

            synchronized (shared) { 

                while (!shared.a1Done) { 

                    shared.wait(); // Passive wait for A1 

                } 

            } 

            shared.B2 = shared.A1 + MathUtility.calculateSum(200); 

            synchronized (shared) { 

                shared.b2Done = true; 

                shared.notifyAll(); // Signal A2 that B2 is ready 

            } 


            // FuncB3 

            synchronized (shared) { 

                while (!shared.a2Done) { 

                    shared.wait(); // Passive wait for A2 

                } 

            } 

            shared.B3 = shared.A2 + MathUtility.calculateSum(400); 

            synchronized (shared) { 

                shared.b3Done = true; 

                shared.notifyAll(); // Signal A3 that B3 is ready 

            } 

        } catch (InterruptedException e) { 

            Thread.currentThread().interrupt(); 

        } 

    } 

} 



 //  Main/driver application class 

 
public class ThreadSyncApp { 

    public static void main(String[] args) { 

        int iterations = 10000; 

        int failedIterations = 0; 
        

        System.out.println("Starting synchronization test for " + iterations + " iterations..."); 

 
        for (int i = 0; i < iterations; i++) { 

            SharedMemory shared = new SharedMemory(); 

            ThreadA ta = new ThreadA(shared); 

            ThreadB tb = new ThreadB(shared); 


            ta.start(); 

            tb.start(); 


            try { 

                // Main thread waits for both A and B to complete 

                ta.join(); 

                tb.join(); 

            } catch (InterruptedException e) { 

                e.printStackTrace(); 

            } 


            // Verify correctness based on mathematical expectations 

            if (shared.A3 != 350900 || shared.B3 != 270700 || shared.B1 != 31375) { 

                failedIterations++; 

            } 

        } 

 

        System.out.println("Test completed."); 

        System.out.println("Failed iterations (Race conditions detected): " + failedIterations); 

         

        if (failedIterations == 0) { 

            System.out.println("SUCCESS: Synchronization code verified. Execution order was strictly maintained."); 

        } 

    } 

} 