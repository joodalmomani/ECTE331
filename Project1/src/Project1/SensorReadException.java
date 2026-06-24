package Project1;


import java.io.IOException; 


// Custom exception representing a hardware or read failure from a sensor. 

public class SensorReadException extends IOException { 

    public SensorReadException(String message) { 

        super(message); 

    } 

} 