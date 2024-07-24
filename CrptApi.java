import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final int requestLimit;
    private final AtomicInteger currentNumberOfRequests = new AtomicInteger(0);
    private volatile long startTime = Long.MIN_VALUE;
    private volatile long finishTime;
    private final Object lock = new Object();
    private final long interim;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        interim = TimeUnit.NANOSECONDS.convert(1L, timeUnit);
    }

    public void create(CrptApi.Document document, String sign) {

        checkNumberOfRequestsAndBlock();

    }

    private void checkNumberOfRequestsAndBlock() {
        long currentTime = System.nanoTime();
        if (startTime <= currentTime && currentTime <= finishTime) {
            synchronized (lock) {
                boolean isInterrupted = false;
                while (currentNumberOfRequests.intValue() >= requestLimit && !isInterrupted) {

                    if (startTime <= currentTime && currentTime < finishTime) {

                        long waitTime = finishTime - currentTime;
                        try {
                            TimeUnit.NANOSECONDS.timedWait(lock, waitTime);
                        } catch (InterruptedException e) {
                            break;
                        }
                        currentTime = System.nanoTime();
                    } else {
                        setTimesAndNumber(currentTime);
                    }
                }
            }
        } else {
            setTimesAndNumber(currentTime);
        }

        currentNumberOfRequests.getAndIncrement();
    }

    private void setTimesAndNumber(long currentTime) {
        startTime = currentTime;
        finishTime = startTime + interim;
        currentNumberOfRequests.set(0);
    }

    static class Document {
        ParticipantInn description = new ParticipantInn();
        String doc_id = "";
        String doc_status = "";
        String doc_type = "";
        boolean importRequest;
        String owner_inn = "";
        String participant_inn = "";
        String producer_inn = "";
        LocalDate production_date;
        String production_type = "";
        List<Product> products = new ArrayList<>();
        LocalDate reg_date;
        String reg_number = "";
    }

    static class ParticipantInn {
        String participantInn = "";
    }

    static class Product {
        String certificate_document = "";
        LocalDate certificate_document_date;
        String certificate_document_number = "";
        String owner_inn = "";
        String producer_inn = "";
        LocalDate production_date;
        String tnved_code = "";
        String uit_code = "";
        String uitu_code = "";
    }

}
