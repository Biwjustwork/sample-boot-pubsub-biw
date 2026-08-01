package th.mfu.activity;

import org.springframework.data.repository.CrudRepository;

public interface ActivityRepository extends CrudRepository<MemberActivity, Long> {


    MemberActivity findByName(String name);
}
