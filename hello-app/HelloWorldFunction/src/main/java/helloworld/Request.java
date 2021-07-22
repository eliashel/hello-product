package helloworld;

public class Request {
    String message;
    int timeToSleep;




    
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getTimeToSleep() {
        return timeToSleep;
    }
    public void setTimeToSleep(int timeToSleep) {
        this.timeToSleep = timeToSleep;
    }


    @Override
    public String toString() {
        return "Request [message=" + message + ", timeToSleep=" + timeToSleep + "]";
    }

    
}
