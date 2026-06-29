package Project3; 

import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter; 


/** 
 * Demonstrates the Priority Ceiling real-time protocol. 
 * This simulation forces a low-priority thread to instantly inherit 
 * the highest possible resource priority upon acquiring a lock,
 * proactively preventing preemption by medium-priority threads.
 */ 

public class Task5_CeilingDemo {  

	/** 
     * Generates a formatted timestamp for precise synchronization event logging. 
     * @return A String representing the current system time in HH:mm:ss.SSS format. 
     */ 
    private static String getTime() { 
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")); 
    } 

    /** 
     * A custom lock manager representing a mutually exclusive hardware resource. 
     * Enforces the Priority Ceiling protocol to systematically eliminate priority inversion. 
     */ 
    static class CeilingMotorController {  
    	/** The maximum priority ceiling defined for this specific hardware resource. */ 
        private final int CEILING_PRIORITY = 8;  
        /** Tracks the thread currently holding the hardware lock. */ 
        private Thread currentOwner = null;  

        /** 
         * Attempts to acquire the mutually exclusive hardware lock. 
         * If the lock is available, the thread's priority is immediately elevated  
         * to the resource's predefined ceiling limit before executing the critical section. 
         * @param threadName The descriptive name of the thread requesting access. 
         */ 
        public void accessMotor(String threadName) {  
            Thread callingThread = Thread.currentThread();  
            int originalPriority = callingThread.getPriority(); 

 

            // 1. Synchronized state-check block 
            synchronized(this) { 
                while (currentOwner != null && currentOwner != callingThread) {  
                    try { wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } 
                }  
                currentOwner = callingThread;  

                if (originalPriority < CEILING_PRIORITY) { 

                    System.out.println("[" + getTime() + "] [CEILING] Elevating " + threadName + "'s priority to ceiling: " + CEILING_PRIORITY); 
                    callingThread.setPriority(CEILING_PRIORITY); 
                } 
            }  


            // 2. Simulated Critical Section  
            System.out.println("[" + getTime() + "] [LOCK EVENT] " + threadName + " executing at ceiling priority.");  

            try {  
                Thread.sleep(1000);  
            } catch (InterruptedException e) {  
                Thread.currentThread().interrupt();  
            }  

            System.out.println("[" + getTime() + "] [LOCK EVENT] " + threadName + " released MotorController lock.");  

 

            // 3. Synchronized release block 
            synchronized(this) { 
                if (callingThread.getPriority() != originalPriority) { 
                    System.out.println("[" + getTime() + "] [CEILING] Restoring " + threadName + "'s priority back to " + originalPriority); 
                    callingThread.setPriority(originalPriority); 
                } 
                currentOwner = null;  
                notifyAll();  
            } 
        }  
    }  

 
    /** 
     * The main execution method. initiates the creation and staggering 
     * of the Logger, Safety Monitor, and Motion Planner threads to demonstrate 
     * the proactive preemption prevention of the ceiling protocol.
     * @param args Command-line arguments (unused). 
     * @throws InterruptedException if the main thread is interrupted while waiting for thread joins. 
     */ 
    public static void main(String[] args) throws InterruptedException {  
        System.out.println("[" + getTime() + "] ----> Starting Task 5: Priority Ceiling");  
        final CeilingMotorController motor = new CeilingMotorController();  

        final int PRIORITY_LOW = 2;  
        final int PRIORITY_MED = 5;  
        final int PRIORITY_HIGH = 8;  

        Thread logger = new Thread(() -> {  
            System.out.println("[" + getTime() + "] [EXEC] Logger (Low) started.");  
            motor.accessMotor("Logger");  
            System.out.println("[" + getTime() + "] [EXEC] Logger (Low) finished.");  
        }, "LoggerThread");  
        logger.setPriority(PRIORITY_LOW);  

 
        Thread safetyMonitor = new Thread(() -> {  
            System.out.println("[" + getTime() + "] [EXEC] Safety Monitor (High) started. Awaiting lock.");  

            long requestTime = System.currentTimeMillis();  
            motor.accessMotor("Safety Monitor");  
            long completionTime = System.currentTimeMillis(); 

            long responseTime = completionTime - requestTime;  
            long waitingTime = responseTime - 1000;  

             

            System.out.println("[" + getTime() + "] [EXEC] Safety Monitor (High) finished.");  
            System.out.println("\n----> PERFORMANCE METRICS <----"); 
            System.out.println("Timestamps -> Requested: " + requestTime + " | Completed: " + completionTime); 
            System.out.println("Waiting Time: " + waitingTime + " ms");  
            System.out.println("Response Time: " + responseTime + " ms\n");  
        }, "SafetyMonitorThread");  

        safetyMonitor.setPriority(PRIORITY_HIGH);  

 
        Thread motionPlanner = new Thread(() -> {  
            System.out.println("[" + getTime() + "] [EXEC] Motion Planner (Medium) started. Heavy compute.");  
            long count = 0;  
            while (count < 3000000000L) count++;  
            System.out.println("[" + getTime() + "] [EXEC] Motion Planner (Medium) finished.");  
        }, "MotionPlannerThread");  

        motionPlanner.setPriority(PRIORITY_MED);  


        logger.start();  
        Thread.sleep(100);  
        safetyMonitor.start();  
        Thread.sleep(100);  
        motionPlanner.start();  

 
        logger.join();  
        motionPlanner.join();  
        safetyMonitor.join();  
    }  
} 