/**
 * Created by Drake Nelson on 3/5/2017.
 * The Great American Hospital Simulation
 * Add events to queue : arrivals treatments and deaths
 * on arrival add them to a treatment queue
 * on treatment grab someone from treatment queue
 * on death mark them as inelligble for treatment
 *
 * keep clock and time limit constant
 * put timestamps on events, comparator of the timestamp and give the queue addInOrder()
 *
 * loop through clock while events generate other events
 * arrival -> arrival and death
 * treatment -> treatment
 *
 */
public class Main {
    public static void main(String[] args) {
        Hospital hospital=new Hospital(100,1);
        hospital.runSimulation();
        hospital.printStats();

       hospital=new Hospital(100,2);
       hospital.runSimulation();
       hospital.printStats();


    }
}
