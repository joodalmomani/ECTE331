 
package Project3; 

 
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter; 


/** * Demonstrates the priority inheritance protocol.  

 */  

public class Task4_InheritanceDemo {  
	
    private static String getTime() { 
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")); 
    } 

     
    static class InheritingMotorController {  
        private Thread currentOwner = null;  
        private int originalPriority;  

        public void accessMotor(String threadName, int callerPriority) {  
            Thread callingThread = Thread.currentThread();  

 
            // 1. Synchronized state-check block 
            synchronized(this) { 
                while (currentOwner != null && currentOwner != callingThread) {  
                    if (currentOwner.getPriority() < callerPriority) {  
                        System.out.println("[" + getTime() + "] [INHERITANCE] High-priority blocked! Upgrading " + currentOwner.getName() +   

                                           "'s priority from " + currentOwner.getPriority() + " to " + callerPriority);  
                        currentOwner.setPriority(callerPriority);  
                    }  
                    try {  
                        wait();  
                    } catch (InterruptedException e) {  
                        Thread.currentThread().interrupt();  
                    }  
                }  
                currentOwner = callingThread;  
                originalPriority = callingThread.getPriority();  
            }  

 
            // 2. Simulated Critical Section  
            System.out.println("[" + getTime() + "] [LOCK EVENT] " + threadName + " acquired MotorController lock.");  
            try {  
                Thread.sleep(1000);  
            } catch (InterruptedException e) {  
                Thread.currentThread().interrupt();  
            }  


            System.out.println("[" + getTime() + "] [LOCK EVENT] " + threadName + " released MotorController lock.");  

            
            // 3. Synchronized release block 
            synchronized(this) { 
                if (callingThread.getPriority() != originalPriority) {  
                    System.out.println("[" + getTime() + "] [INHERITANCE] Restoring " + threadName + "'s priority back to " + originalPriority);  

                    callingThread.setPriority(originalPriority);  
                }  
                currentOwner = null;  
                notifyAll();  
            } 
        }  
    }  

 

    public static void main(String[] args) throws InterruptedException {  
        System.out.println("[" + getTime() + "] ----> Starting Task 4: Priority Inheritance");  
        final InheritingMotorController motor = new InheritingMotorController();  

 
        final int PRIORITY_LOW = 2;  
        final int PRIORITY_MED = 5;  
        final int PRIORITY_HIGH = 8;  

 
        Thread logger = new Thread(() -> {  
            System.out.println("[" + getTime() + "] [EXEC] Logger (Low) started.");  
            motor.accessMotor("Logger", PRIORITY_LOW);  
            System.out.println("[" + getTime() + "] [EXEC] Logger (Low) finished.");  
        }, "LoggerThread");  

        logger.setPriority(PRIORITY_LOW);  

 
        Thread safetyMonitor = new Thread(() -> {  
            System.out.println("[" + getTime() + "] [EXEC] Safety Monitor (High) started. Awaiting lock.");  

            long requestTime = System.currentTimeMillis();  
            motor.accessMotor("Safety Monitor", PRIORITY_HIGH);  
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