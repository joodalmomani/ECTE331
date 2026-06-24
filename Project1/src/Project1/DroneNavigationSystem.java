package Project1;



import java.io.FileWriter; 

import java.io.IOException; 

import java.io.PrintWriter; 

import java.time.LocalDateTime; 

import java.time.format.DateTimeFormatter; 

import java.util.ArrayList; 

import java.util.List; 

import java.util.Random; 

 



 //this class simulates a fault-tolerant autonomous drone navigation system 
 //TMR voting used for altitude estimation  


public class DroneNavigationSystem { 

 

    private static final int MIN_VALID_ALT = 0; 

    private static final int MAX_VALID_ALT = 200; 

    private static int previousValidAltitude = 100; // Baseline starting altitude 

    private static int consecutiveFailures = 0; 

    
 
    //simulated sensor reading based on probability rules is generated

     // @param sensorId The identifier of the sensor (e.g., "A", "B", "C") ;   @return An integer representing the altitude reading ;  @throws SensorReadException if the sensor simulates a total failure (0-14 chance)


    public static int readSensor(String sensorId) throws SensorReadException { 

        Random random = new Random(); 

        int chance = random.nextInt(100); // 0 to 99 inclusive 

 

        if (chance < 15) { 

            throw new SensorReadException("Sensor " + sensorId + " failure"); 

        } else if (chance < 30) { 

            // Corrupted reading (outside valid [0:200] range) 

            return MAX_VALID_ALT + 1 + random.nextInt(100);  

        } else { 

            // Valid reading 

            return random.nextInt(MAX_VALID_ALT + 1); 

        } 

    } 

 

    

     //handles file logging for all critical system events. 

     // @param eventType The category of the event (e.g., "Majority decision") ;   @param details Specific details including outlier sensor IDs if applicable


    private static void logEvent(String eventType, String details) { 

        try { 

            // "true" appends to the file instead of overwriting it 

            FileWriter fw = new FileWriter("log.txt", true); 

            PrintWriter pw = new PrintWriter(fw); 

             

            LocalDateTime now = LocalDateTime.now(); 

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 

             

            String logEntry = "[" + now.format(formatter) + "] " + eventType + " - " + details; 

            pw.println(logEntry); 

             

            // Mirroring output to console 

            System.out.println(logEntry); 
             

            pw.close(); 

        } catch (IOException e) { 

            e.printStackTrace(); 

        } 

    } 

 

    

     // main method -> executing the drone flight simulation. 

     // @param args Command line arguments 

    public static void main(String[] args) { 

        System.out.println("--->Initializing Autonomous Drone Navigation System "); 

        int totalSimulationCycles = 15; 

        // Loop placed inside the try block to catch the SystemReliabilityException and trigger the SAFE MODE transition. 

        try { 

            for (int cycle = 1; cycle <= totalSimulationCycles; cycle++) { 

                System.out.println("\n Cycle: " + cycle ); 

                Integer valA = null, valB = null, valC = null; 

                List<String> outliers = new ArrayList<>(); 

                int validCount = 0; 

                // 1. Sensor Reading and Fault Detection 

                try { 

                    valA = readSensor("A"); 

                    if (valA >= MIN_VALID_ALT && valA <= MAX_VALID_ALT) { 

                        validCount++; 

                    } else { 

                        logEvent("Corrupted reading", "Sensor A reading out of bounds: " + valA); 

                        outliers.add("A"); 

                    } 

                } catch (SensorReadException e) { 

                    logEvent("Sensor failure", e.getMessage()); 

                    outliers.add("A"); 

                } 

 

                try { 

                    valB = readSensor("B"); 

                    if (valB >= MIN_VALID_ALT && valB <= MAX_VALID_ALT) { 

                        validCount++; 

                    } else { 

                        logEvent("Corrupted reading", "Sensor B reading out of bounds: " + valB); 

                        outliers.add("B"); 

                    } 

                } catch (SensorReadException e) { 

                    logEvent("Sensor failure", e.getMessage()); 

                    outliers.add("B"); 

                } 

 

                try { 

                    valC = readSensor("C"); 

                    if (valC >= MIN_VALID_ALT && valC <= MAX_VALID_ALT) { 

                        validCount++; 

                    } else { 

                        logEvent("Corrupted reading", "Sensor C reading out of bounds: " + valC); 

                        outliers.add("C"); 

                    } 

                } catch (SensorReadException e) { 

                    logEvent("Sensor failure", e.getMessage()); 

                    outliers.add("C"); 

                } 

 

                System.out.println("Sensor Values -> A: " + valA + " | B: " + valB + " | C: " + valC); 

 

                //2. Reliability and Majority Voting Rule 

                boolean cycleFailed = false; 

                if (validCount < 2) { 

                    logEvent("Reliability status", "Failure. Fewer than 2 valid sensors."); 

                    cycleFailed = true; 

                } else { 

                    // Extract safe values for comparison (-1 for invalid/null data) 

                    int a = (valA != null && valA <= MAX_VALID_ALT) ? valA : -1; 

                    int b = (valB != null && valB <= MAX_VALID_ALT) ? valB : -1; 

                    int c = (valC != null && valC <= MAX_VALID_ALT) ? valC : -1; 

 

                    // TMR Voting Logic 

                    if (a == b && a != -1) { 

                        previousValidAltitude = a; 

                        if (c != a && c != -1) outliers.add("C"); 

                        logEvent("Majority decision", "Altitude set to " + a); 

                    } else if (b == c && b != -1) { 

                        previousValidAltitude = b; 

                        if (a != b && a != -1) outliers.add("A"); 

                        logEvent("Majority decision", "Altitude set to " + b); 

                    } else if (a == c && a != -1) { 

                        previousValidAltitude = a; 

                        if (b != a && b != -1) outliers.add("B"); 

                        logEvent("Majority decision", "Altitude set to " + a); 

                    } else { 

                        // All valid sensors output different values 

                        logEvent("Fallback decision", "No majority found. Using previous altitude: " + previousValidAltitude); 

                        cycleFailed = true;  

                    } 

                } 

 

                // 3. Outlier Logging 

                if (!outliers.isEmpty()) { 

                    logEvent("Outlier detection", "Outlier sensor(s) ID: " + String.join(", ", outliers)); 

                } 

                
                // 4. Reliability Tracking 

                if (cycleFailed) { 

                    consecutiveFailures++; 

                    if (consecutiveFailures >= 2) { 

                        throw new SystemReliabilityException("Two consecutive system failures detected."); 

                    } 

                } else { 

                    consecutiveFailures = 0; // Reset counter on successful cycle 

                } 

 

                // Pause to simulate processing time between read cycles 

                Thread.sleep(250);  

            } 

 

        } catch (SystemReliabilityException e) { 

            System.out.println("\n========================================"); 

            logEvent("SAFE MODE activation", e.getMessage() + " Halting execution."); 

            System.out.println("========================================"); 

        } catch (InterruptedException e) { 

            System.out.println("Thread execution interrupted."); 

        } 

    } 

} 