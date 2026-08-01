package th.mfu.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * activity-service. Port 8203. The THIRD subscriber.
 * <p>
 * Nothing to change in this file - the work of step 3 is in
 * BorrowPlacedListener.
 */
@SpringBootApplication
public class ActivityServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(ActivityServiceApp.class, args);
    }
}
