/*
 Hospital class is the driver for the simulation
 purpose: runSimulation();
 hospitals have :
 a queue of patient
 a clock that keeps track of seconds
 a time limit for simulation
 a queue of events with timestamps/type
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


class Hospital {
    Hospital(int hours, int numOfDoctors) {
        clock = 0;
        END_SIMULATION = hours * 60 * 60;
        arrivalCount = 0;
        doctorCount = numOfDoctors;
        stats = new simulationSummary();
    }


    private simulationSummary stats;
    private int doctorCount;

    void printSimulation() {
        System.out.println("THE GREAT AMERICAN MEDICAL SYSTEM SIMULATION");
        System.out.println("This run is for " + doctorCount + " doctors on staff\n");
        int x = 0;
        for (Patient ignored : patientQueue) {
            x++;
        }
        stats.printStats();
        System.out.println(x + " patients left in the hospital when shut down\n");
    }

    void runSimulation() {
        hospitalEventQueue.add(new HospitalEvent(0));
        hospitalEventQueue.add(new HospitalEvent(1, Event_Type.TREATMENT));
        while (clock < END_SIMULATION) {
            HospitalEvent event = hospitalEventQueue.poll();
            event.execute();
        }
        stats.setAverages();
    }

    private int clock = 0;//every second of the simulation
    private final int END_SIMULATION;// = 360000;//seconds = 100 hours

    private int arrivalCount = 0;//count the number of patients that arrive at the hospital

    //:patintQueue is a que with a special comparator to maintain a sort
    private PriorityQueue<Patient> patientQueue = new PriorityQueue<>(10, new PatientComparator());

    //comparator:
    //heart takes priority over not heart
    //if both heart || both !heart give priority to existing item in list
    private class PatientComparator implements Comparator<Patient> {
        @Override
        public int compare(Patient x, Patient y) {
            if (x.getAilment() == Ailment.HEART) {
                if (y.getAilment() == Ailment.HEART) {
                    return 1;
                } else {
                    if (x.arrivalTime > y.arrivalTime) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            } else {
                return 1;
            }
        }
    }

    //:hospitalEventQueue is a que with a special comparator
    private PriorityQueue<HospitalEvent> hospitalEventQueue = new PriorityQueue<>(10, new HospitalEventComparator());

    //comparator gives priority to HospitalEvents based on time they will happen
    private class HospitalEventComparator implements Comparator<HospitalEvent> {
        @Override
        public int compare(HospitalEvent x, HospitalEvent y) {
            if (x.getTime() < y.getTime()) {
                return -1;
            }
            if (x.getTime() > y.getTime()) {
                return 1;
            } else if (x.getType() == Event_Type.ARRIVAL) {
                return 1;
            } else if (x.getType() == Event_Type.DEATH) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    //Hospital events consist of a type of event, patient participating in the event, and the time it will happen
    private class HospitalEvent {
        private int time;
        private Event_Type type;
        private Patient patient;

        //this is for arrival events only
        HospitalEvent(int time) {
            this.time = clock + time;
            patient = null;
            this.type = Event_Type.ARRIVAL;
        }

        HospitalEvent(int time, Event_Type type, Patient patient) {
            this.patient = patient;
            this.time = time;
            this.type = type;
        }

        HospitalEvent(int time, Event_Type type) {
            patient = null;
            this.time = time;
            this.type = type;
        }

        int getTime() {
            return time;
        }

        Event_Type getType() {
            return type;
        }

        void execute() {
            switch (type) {
                case ARRIVAL:
                    clock = time;
                    boolean nextScheduled = false;
                    for (HospitalEvent e : hospitalEventQueue) {
                        if (e.getType() == Event_Type.TREATMENT) {
                            nextScheduled = true;
                        }
                    }
                    if (patientQueue.isEmpty() && !nextScheduled) {
                        hospitalEventQueue.add(new HospitalEvent(clock, Event_Type.TREATMENT));
                    }
                    arrivalCount++;
                    patient = new Patient(clock, arrivalCount);
                    patientQueue.add(patient);
                    //testing line: System.out.println("patient arrived at " + patient.arrivalTime + " time " + patient+"with"+patient.getAilment());
                    hospitalEventQueue.add(new HospitalEvent(patient.getDeathTime(), Event_Type.DEATH, patient));
                    hospitalEventQueue.add(new HospitalEvent(timeUntilNextArrival()));
                    break;
                case DEATH:
                    patientQueue.remove(patient);
                    if (!patient.wasTreated) {
                        patient.killPatient(time);
                        stats.addDeath(patient);
                        // testing print line System.out.println("patient has died RIP: " + patient);
                    }
                    break;
                case TREATMENT:
                    if (patientQueue.isEmpty()) {
                        break;
                    } else {
                        Patient treatmentPatient = patientQueue.poll();
                        if (treatmentPatient.isAlive) {
                            int length = getTreatmentTime(treatmentPatient.getAilment());
                            treatmentPatient.treatPatient(time, length);
                            clock = time += length;
                            hospitalEventQueue.add(new HospitalEvent(clock, Event_Type.TREATMENT));
                            // testing line needs removed System.out.println("treated a patient: " + treatmentPatient);
                            stats.treatPatient(treatmentPatient);
                        } else {
                            System.err.println("tried to treat a dead patient");
                        }
                        break;
                    }
                default:
                    System.err.println("bugs bug bugs HospitalEvent execute()");
                    break;
            }
        }

        //uses negative exponential distrobution
        private int getTreatmentTime(Ailment ailment) {
            double uniformRandomNumber = Math.random();

            int length = 0;
            switch (ailment) {
                case BLEED:
                    length = (int) (3600 * Math.log(1 - uniformRandomNumber) / (-6 * doctorCount));
                    break;
                case HEART:
                    length = (int) (3600 * Math.log(1 - uniformRandomNumber) / (-2 * doctorCount));
                    break;
                case GAS:
                    length = (int) (3600 * Math.log(1 - uniformRandomNumber) / (-4 * doctorCount));
                    break;
                default:
                    System.err.println("error in treatment case");
                    break;
            }
            return length;
        }

        //use poisson distro for next arrival
        private int timeUntilNextArrival() {
            double nextArrival = Math.log(1 - Math.random()) / -3.0;
            return (int) (nextArrival * 3600);
        }
    }

    private class simulationSummary {
        //death counts per ailment and total
        int deathsByGas, deathsByHeart, deathsByBlood, totalDeaths;
        //treat counts per ailment and total
        int treatedGas, treatedHeart, treatedBlood, treatedTotal;
        //average time in que per ailment and total
        int avgTimeInQueGas, avgTimeInQueHeart, avgTimeInQueBlood, avgTimeInQueTotal;
        //total time in hospital per ailment and total
        int totalGasTimeInQue = 0, totalBloodTimeInQue = 0, totalHeartTimeInQue = 0, totalTimeInQue = 0;

        //lists of patients
        private List<Patient> deadPatientList;
        private List<Patient> treatedPatientList;

        simulationSummary() {
            deathsByGas = 0;
            deathsByHeart = 0;
            deathsByBlood = 0;
            totalDeaths = 0;
            treatedGas = 0;
            treatedHeart = 0;
            treatedBlood = 0;
            treatedTotal = 0;
            avgTimeInQueGas = 0;
            avgTimeInQueHeart = 0;
            avgTimeInQueBlood = 0;
            avgTimeInQueTotal = 0;
            deadPatientList = new ArrayList<>();
            treatedPatientList = new ArrayList<>();
        }

        void addDeath(Patient patient) {
            int timeInQue = patient.deathTime - patient.arrivalTime;
            switch (patient.getAilment()) {
                case BLEED:
                    deathsByBlood++;
                    totalBloodTimeInQue += timeInQue;
                    break;
                case HEART:
                    deathsByHeart++;
                    totalHeartTimeInQue += timeInQue;
                    break;
                case GAS:
                    deathsByGas++;
                    totalGasTimeInQue += timeInQue;
                    break;
                default:
                    System.out.println("death error");
                    break;
            }
            totalDeaths++;
            totalTimeInQue += timeInQue;
            deadPatientList.add(patient);
        }

        private void treatPatient(Patient patient) {
            int timeInQue = patient.treatmentTime - patient.arrivalTime;
            switch (patient.getAilment()) {
                case BLEED:
                    treatedBlood++;
                    totalBloodTimeInQue += timeInQue;
                    break;
                case HEART:
                    treatedHeart++;
                    totalHeartTimeInQue += timeInQue;
                    break;
                case GAS:
                    treatedGas++;
                    totalGasTimeInQue += timeInQue;
                    break;
                default:
                    System.out.println("treatment error");
                    break;
            }
            treatedTotal++;
            totalTimeInQue += timeInQue;
            treatedPatientList.add(patient);
        }

        private void printStats() {
            System.out.printf("Losses of Gastro:\t%d\nLosses of Heart:\t%d\nLosses of Bleeders:\t%d\nTotal Losses:\t%d\n\n", deathsByGas, deathsByHeart, deathsByBlood, totalDeaths);
            System.out.printf("Number of Gastro Serviced:\t%d\nNumber of Hearts Serviced:\t%d\nNumber of Bleeders Serviced:\t%d\nTotal Serviced:\t%d\n\n", treatedGas, treatedHeart, treatedBlood, treatedTotal);
            System.out.printf("Average time Gastro spend in queue:\t%d\nAverage time Heart Patients spend in queue:\t%d\nAverage time Bleeders spend in queue:\t%d\nAverage Time any patient spent in queue:\t%d\n\n", avgTimeInQueGas, avgTimeInQueHeart, avgTimeInQueBlood, avgTimeInQueTotal);
            System.out.println("Patients Lost during the simulation");
//            for (Patient patient : deadPatientList) {
//                System.out.println(patient);
//            }
          //  System.out.println("Patients Treated during the simulation");
          //  for (Patient patient : treatedPatientList) {
          //      System.out.println(patient);
          //  }
        }

        private void setAverages() {
            avgTimeInQueBlood = (totalBloodTimeInQue / (deathsByBlood + treatedBlood));
            avgTimeInQueGas = (totalGasTimeInQue / (deathsByGas + treatedGas));
            avgTimeInQueHeart = (totalHeartTimeInQue / (deathsByHeart + treatedHeart));
            avgTimeInQueTotal = (totalTimeInQue / (totalDeaths + treatedTotal));
        }
    }
}