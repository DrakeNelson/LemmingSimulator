import java.util.Formatter;
import java.util.Locale;

/**
 * setAilment and setDeathTime methods make use of RNG
 */
public class Patient {
    private Ailment ailment;
    boolean isAlive;
    boolean wasTreated;
    int arrivalTime;
    private int deathTime;
    private int treatmentTime;
    private int treatmentLength;
    private int patientNumber;

    Patient(int arrivalTime, int patientNumber) {
        this.arrivalTime = arrivalTime;
        this.patientNumber = patientNumber;
        isAlive = true;
        wasTreated = false;
        setAilment();
        setDeathTime();
    }

    //patient ailment is uniformly distributed
    private void setAilment() {
        int x = ((int) (Math.random() * 10));
        if(x<3)
            ailment = Ailment.HEART;
        else if(x<5)
            ailment = Ailment.BLEED;
        else        ailment = Ailment.GAS;

    }

    //use standard deviation for the times people die
    private void setDeathTime() {
        double time = 0.0;
        for(int i = 0; i < 12; i++){
            time += Math.random();
        }
        time = time - 6;
        double mu=0.0;
        double sigma=0.0;

        switch (ailment) {
            case BLEED:
                mu = 65.0*60;
                sigma = 1.0/3;
                break;
            case GAS:
                mu = 80.0*60;
                sigma = 1.0/2;
                break;
            case HEART:
                mu = 35.0*60;
                sigma = 1.0/6;
                break;
            default:
                System.err.println("case bug in lifetime in waiting line");
                break;
        }
        time = (sigma*time + mu);
        deathTime=arrivalTime+(int)time;
    }

    Ailment getAilment() {
        return ailment;
    }

    //use Alive get/set to tell whether a person is dead
    int getDeathTime() {
        return deathTime;
    }

    void killPatient(int time) {
        isAlive = false;
        deathTime = time;
    }

    void treatPatient(int time, int length) {
        treatmentTime = time;
        treatmentLength = length;
        wasTreated = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        if (wasTreated) {
            formatter.format("ID:\t%4d\tAilment:\t%8s\tArrival Time:\t%8d\tTreatment Began:\t%8d\tTreatment Lasted:\t%5d\tTotal Time In Hospital:\t%8s", patientNumber, ailment, arrivalTime, treatmentTime, treatmentLength, (treatmentLength + treatmentTime - arrivalTime));
        } else if (!isAlive) {
            formatter.format("ID:\t%4d\tAilment:\t%8s\tArrival Time:\t%8d\tDeath Time:\t%8d\tTotal Time In Hospital:\t%8s", patientNumber, ailment, arrivalTime, deathTime, (deathTime - arrivalTime));
        } else {
            formatter.format("ID:\t%4d :: alive and untreated", patientNumber);
        }

        return sb.toString();
    }
}
