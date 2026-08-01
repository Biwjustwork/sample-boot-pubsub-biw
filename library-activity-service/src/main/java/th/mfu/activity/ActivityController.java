package th.mfu.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read side: the page at http://localhost:8203/ calls this every two seconds.
 * Already finished - nothing to do here.
 */
@RestController
public class ActivityController {

    @Autowired
    private ActivityRepository popularityRepository;

    @GetMapping("/activity")
    public Iterable<MemberActivity> listPopularity() {
        return popularityRepository.findAll();
    }
}
