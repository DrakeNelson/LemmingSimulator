/*
 Hospital class is the driver for the simulation
 purpose: runSimulation();
 hospitals have :
 a queue of patient
 a clock that keeps track of seconds
 a time limit for simulation
 a queue of events with timestamps/type
 */

import java.util.Comparator;
import java.util.PriorityQueue;


class Hospital {
    Hospital(int hours, int numOfDoctors) {
        clock = 0;
        END_SIMULATION = hours * 60 * 60;
        arrivalCount = 0;
        deathCount = 0;
        treatmentCount = 0;
        doctorCount = numOfDoctors;
    }

    private int doctorCount;

    void printStats() {
        System.out.println(arrivalCount + " arrived \n" + deathCount + " died\n" + treatmentCount + " treated\n");
        int x = 0;
        for (Patient ignored : patientQueue) {
            x++;
        }
        System.out.println(x + "patients left in the hospital when shut down");
    }

    void runSimulation() {
        hospitalEventQueue.add(new HospitalEvent(0));
        hospitalEventQueue.add(new HospitalEvent(1, Event_Type.TREATMENT));
        while (clock < END_SIMULATION) {
            HospitalEvent event = hospitalEventQueue.poll();
            event.execute();
        }
    }

    private int clock = 0;//every second of the simulation
    private final int END_SIMULATION;// = 360000;//seconds = 100 hours

    private int arrivalCount = 0;//count the number of patients that arrive at the hospital
    private int deathCount = 0;//count the number of patients that die in the simulation
    private int treatmentCount = 0;//count the number of patients that are treated in the simulation

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
                        //todo counts/stats
                        System.out.println("patient has died RIP: " + patient);
                        deathCount++;
                    }
                    break;
                case TREATMENT:
                    if (patientQueue.isEmpty()) {
                        break;
                    } else {
                        treatmentCount++;
                        Patient treatmentPatient = patientQueue.poll();
                        if (treatmentPatient.isAlive) {
                            int length = getTreatmentTime(treatmentPatient.getAilment());
                            treatmentPatient.treatPatient(time, length);
                            clock = time += length;
                            hospitalEventQueue.add(new HospitalEvent(clock, Event_Type.TREATMENT));
                            System.out.println("treated a patient: " + treatmentPatient);
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
        //death counts per
    }
}