package Project3;


//TASK3: unbounded priority inversion -> a medium-priority thread delays a high-priority thread by preempting a low-priority lock holder. 

/** 
 * Demonstrates the priority inversion problem in a real-time system. 
 */ 
public class Task3_InversionDemo { 
	
	/** 
	 * Executes the priority inversion demonstration. 
	 * @param args Command-line arguments. 
	 */ 
    public static void main(String[] args) throws InterruptedException { 
        System.out.println("---->Task 3: Priority Inversion "); 

        final MotorController motor = new MotorController(); 
        
        //priority mapping 
        final int PRIORITY_LOW = 2;    // Logger 
        final int PRIORITY_MED = 5;    // Motion Planner 
        final int PRIORITY_HIGH = 8;   // Safety Monitor 

        
        // 1. Logger (Low Priority) 
        Thread logger = new Thread(new Runnable() { 
        	
            @Override 
            public void run() { 
                System.out.println("[EXEC] Logger (Low) started."); 
                motor.accessMotor("Logger"); 
                System.out.println("[EXEC] Logger (Low) finished execution."); 
            } 

        }); 
        logger.setPriority(PRIORITY_LOW); 

 

        // 2. Safety Monitor (High Priority) 
        Thread safetyMonitor = new Thread(new Runnable() { 
            @Override 
            public void run() { 
                System.out.println("[EXEC] Safety Monitor (High) started. Awaiting motor lock."); 
                
                // Track exact waiting time of the high-priority thread 
                long startTime = System.currentTimeMillis(); 
                motor.accessMotor("Safety Monitor"); 
                long endTime = System.currentTimeMillis(); 
                long waitingTime = endTime - startTime; 

                System.out.println("[EXEC] Safety Monitor (High) finished execution."); 
                System.out.println("\n---> PERFORMANCE METRIC: Safety Monitor waiting time = " + waitingTime + " ms \n"); 

            } 

        }); 

        safetyMonitor.setPriority(PRIORITY_HIGH); 

 

        // 3. Motion Planner (Medium Priority) 
        Thread motionPlanner = new Thread(new Runnable() { 
            @Override 
            public void run() { 
                System.out.println("[EXEC] Motion Planner (Medium) started. Executing heavy computations."); 

                 
                // Simulate intensive processing without acquiring the lock 
                // This preempts the Low priority thread, causing the inversion 
                long count = 0; 
                while (count < 3000000000L) { 
                    count++; 
                } 

                System.out.println("[EXEC] Motion Planner (Medium) finished execution."); 
            } 

        }); 

        motionPlanner.setPriority(PRIORITY_MED); 

 

        // initiating desired run sequence 

        // FIRST: Low priority acquires the lock first 
        logger.start(); 
        Thread.sleep(100);  


        // SECOND: High priority starts and blocks on the lock held by Low 
        safetyMonitor.start(); 
        Thread.sleep(100);  
        

        // THIRD: Medium priority starts and preempts Low priority, extending the High priority block 
        motionPlanner.start();  

 

        // Wait for all processes to conclude 
        logger.join(); 
        motionPlanner.join(); 
        safetyMonitor.join(); 

         

        System.out.println("----> End of Demonstration "); 

    } 

} 