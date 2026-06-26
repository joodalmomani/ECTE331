package Project3;

 //TASK 5: PRORITY CEILING  

/** 
 * Demonstrates the priority ceiling protocol. 
 */ 
public class Task5_CeilingDemo { 
    // Custom lock manager to have Priority Ceiling 
    static class CeilingMotorController { 
        private final int CEILING_PRIORITY = 8; // Highest priority in the system 
        private Thread currentOwner = null; 

        /** 
         * Simulates priority ceiling while accessing the motor.
         * @param threadName Name of the requesting thread. 
         */ 
        public synchronized void accessMotor(String threadName) { 
            Thread callingThread = Thread.currentThread(); 
            int originalPriority = callingThread.getPriority(); 

            
            while (currentOwner != null && currentOwner != callingThread) { 
                try { wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } 
            } 

 
            // Lock acquired -> instantly elevate to ceiling 
            currentOwner = callingThread; 
            if (originalPriority < CEILING_PRIORITY) { 
                System.out.println("[CEILING] Elevating " + threadName + "'s priority to ceiling: " + CEILING_PRIORITY); 
                callingThread.setPriority(CEILING_PRIORITY); 
            } 


            System.out.println("[LOCK EVENT] " + threadName + " executing at ceiling priority."); 


            try { 
                Thread.sleep(1000); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            } 

 
            System.out.println("[LOCK EVENT] " + threadName + " released MotorController lock."); 

             
            // Restore priority 
            if (callingThread.getPriority() != originalPriority) { 
                System.out.println("[CEILING] Restoring " + threadName + "'s priority back to " + originalPriority); 
                callingThread.setPriority(originalPriority); 
            } 

             
            currentOwner = null; 
            notifyAll(); 
        } 
    }

 
    /** 
     * Executes the priority ceiling demonstration. 
     * @param args Command-line arguments. 
     */ 
    public static void main(String[] args) throws InterruptedException { 
        System.out.println("----> Starting Task 5: Priority Ceiling"); 
        final CeilingMotorController motor = new CeilingMotorController(); 

         
        //setting priorities 
        final int PRIORITY_LOW = 2; 
        final int PRIORITY_MED = 5; 
        final int PRIORITY_HIGH = 8; 

 
        //thread logger
        Thread logger = new Thread(() -> { 
            System.out.println("[EXEC] Logger (Low) started."); 
            motor.accessMotor("Logger"); 
            System.out.println("[EXEC] Logger (Low) finished."); 
        }, "LoggerThread"); 
        logger.setPriority(PRIORITY_LOW); 

 
        
        //safety monitor
        Thread safetyMonitor = new Thread(() -> { 
            System.out.println("[EXEC] Safety Monitor (High) started. Awaiting lock..."); 
            long startTime = System.currentTimeMillis(); 

            motor.accessMotor("Safety Monitor"); 

             
            long waitingTime = System.currentTimeMillis() - startTime; 
            System.out.println("[EXEC] Safety Monitor (High) finished."); 
            System.out.println("\n----> PERFORMANCE: Safety Monitor waiting time = " + waitingTime + " ms\n"); 
        }, "SafetyMonitorThread"); 

        safetyMonitor.setPriority(PRIORITY_HIGH); 


        //motion planner
        Thread motionPlanner = new Thread(() -> { 
            System.out.println("[EXEC] Motion Planner (Medium) started. Heavy compute."); 
            long count = 0; 
            while (count < 3000000000L) count++; 
            System.out.println("[EXEC] Motion Planner (Medium) finished."); 
        }, "MotionPlannerThread"); 

        motionPlanner.setPriority(PRIORITY_MED); 


        // Sequence 
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