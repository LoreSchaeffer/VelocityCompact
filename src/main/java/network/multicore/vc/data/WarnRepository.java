package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

public class WarnRepository extends EntityRepository<Warn, Long> {

    public WarnRepository(EntityManager entityManager, Class<Warn> entityClass) {
        super(entityManager, entityClass);
    }
}
