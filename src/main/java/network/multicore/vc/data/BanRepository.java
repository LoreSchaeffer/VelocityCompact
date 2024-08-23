package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

public class BanRepository extends EntityRepository<Ban, Long> {

    public BanRepository(EntityManager entityManager, Class<Ban> entityClass) {
        super(entityManager, entityClass);
    }
}
