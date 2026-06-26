package Project3;


// Shared resource -> the robotic arm motor critical section. 
/** 
 * Controls synchronized access to the shared robotic arm motor. 
 */ 
public class MotorController { 
    //Synchronized method to ensure mutually exclusive access to the motor. 

	/** 
	 * Grants mutually exclusive access to the motor controller. 
	 * @param threadName Name of the thread requesting access. 
	 */ 
    public synchronized void accessMotor(String threadName) { 

        System.out.println("[LOCK EVENT] " + threadName + " acquired MotorController lock."); 

        try { 
            // Simulate execution time holding the critical section 
            Thread.sleep(1000);  
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        } 

        System.out.println("[LOCK EVENT] " + threadName + " released MotorController lock."); 

    } 

} 