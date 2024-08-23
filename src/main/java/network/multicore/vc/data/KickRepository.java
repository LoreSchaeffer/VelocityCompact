package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

public class KickRepository extends EntityRepository<Kick, Long> {

    public KickRepository(EntityManager entityManager, Class<Kick> entityClass) {
        super(entityManager, entityClass);
    }
}
