package Project1;



 //exception thrown when the system enter SAFE MODE
 //SAFE MODE: entered when consecutive reliability failures  

public class SystemReliabilityException extends Exception { 

    public SystemReliabilityException(String message) { 

        super(message); 

    } 

} 