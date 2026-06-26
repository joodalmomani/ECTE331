package Project1;



 //exception thrown when the system enter SAFE MODE
 //SAFE MODE: entered when consecutive reliability failures  

/** 

 * Custom exception indicating that the system has entered SAFE MODE 

 * due to consecutive reliability failures. 

 */ 

public class SystemReliabilityException extends Exception { 
	
	
	/** 

	 * Creates a new SystemReliabilityException. 

	 * @param message Description of the reliability failure. 

	 */ 

    public SystemReliabilityException(String message) { 

        super(message); 

    } 

} 