package Project3;


//TASK4: Priority Inheritance

/** 
 * Demonstrates the priority inheritance protocol. 
 */ 
public class Task4_InheritanceDemo { 
	
    // Custom lock manager to simulate  Priority Inheritance 
    static class InheritingMotorController { 
        private Thread currentOwner = null; 
        private int originalPriority; 

        /** 
         * Simulates priority inheritance while accessing the motor. 
         * @param threadName Name of the requesting thread. 
         * @param callerPriority Priority of the requesting thread. 
         */ 
        public synchronized void accessMotor(String threadName, int callerPriority) { 
            Thread callingThread = Thread.currentThread(); 

            // If lock is held, check if we need to elevate the owner's priority 
            while (currentOwner != null && currentOwner != callingThread) { 
                if (currentOwner.getPriority() < callerPriority) { 
                    System.out.println("[INHERITANCE] High-priority blocked! Upgrading " + currentOwner.getName() +  
                                       "'s priority from " + currentOwner.getPriority() + " to " + callerPriority); 
                    currentOwner.setPriority(callerPriority); 
                } 

                try { 
                    wait(); 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                } 
            } 

 

            // Lock acquired 
            currentOwner = callingThread; 
            originalPriority = callingThread.getPriority(); 
            System.out.println("[LOCK EVENT] " + threadName + " acquired MotorController lock."); 

            
            try { 
                Thread.sleep(1000); // Simulate critical section 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            } 


            System.out.println("[LOCK EVENT] " + threadName + " released MotorController lock."); 


            // Restore priority upon release 
            if (callingThread.getPriority() != originalPriority) { 
                System.out.println("[INHERITANCE] Restoring " + threadName + "'s priority back to " + originalPriority); 
                callingThread.setPriority(originalPriority); 
            } 

             
            currentOwner = null; 
            notifyAll(); 
        } 
    } 

 
    /** 
     * Executes the priority inheritance demonstration. 
     * @param args Command-line arguments. 
     */ 
    public static void main(String[] args) throws InterruptedException { 
        System.out.println("----> Starting Task 4: Priority Inheritance"); 
        final InheritingMotorController motor = new InheritingMotorController(); 


        final int PRIORITY_LOW = 2; 
        final int PRIORITY_MED = 5; 
        final int PRIORITY_HIGH = 8; 

 
        Thread logger = new Thread(() -> { 
            System.out.println("[EXEC] Logger (Low) started."); 
            motor.accessMotor("Logger", PRIORITY_LOW); 
            System.out.println("[EXEC] Logger (Low) finished."); 
        }, "LoggerThread"); 

        logger.setPriority(PRIORITY_LOW); 

 

        Thread safetyMonitor = new Thread(() -> { 
            System.out.println("[EXEC] Safety Monitor (High) started. Awaiting lock."); 
            long startTime = System.currentTimeMillis(); 

            motor.accessMotor("Safety Monitor", PRIORITY_HIGH); 
            
            
            long waitingTime = System.currentTimeMillis() - startTime; 
            System.out.println("[EXEC] Safety Monitor (High) finished."); 
            System.out.println("\n----> PERFORMANCE: Safety Monitor waiting time = " + waitingTime + " ms \n"); 
        }, "SafetyMonitorThread"); 

        safetyMonitor.setPriority(PRIORITY_HIGH); 


        Thread motionPlanner = new Thread(() -> { 
            System.out.println("[EXEC] Motion Planner (Medium) started. Heavy compute."); 
            long count = 0; 
            while (count < 3000000000L) count++; 
            System.out.println("[EXEC] Motion Planner (Medium) finished."); 
        }, "MotionPlannerThread"); 

        motionPlanner.setPriority(PRIORITY_MED); 

 

        // Sequence that'll force inheritance to happen 
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