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

    //todo implement distrobution
    private void setAilment() {
        int x = ((int) (Math.random() * 3));//Ailment.HEART;
        switch (x) {
            case 0:
                ailment = Ailment.HEART;
                break;
            case 1:
                ailment = Ailment.BLEED;
                break;
            case 2:
                ailment = Ailment.GAS;
                break;
            default:
                System.err.print("bugs on bugs on bugs");
                break;
        }
    }

    //todo implement deviation
    private void setDeathTime() {
        switch (ailment) {
            case BLEED:
                deathTime = arrivalTime + 65 * 60;
                break;
            case GAS:
                deathTime = arrivalTime + 80 * 60;
                break;
            case HEART:
                deathTime = arrivalTime + 35 * 60;
                break;
            default:
                System.err.println("case bug in lifetime in waiting line");
                break;
        }
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
