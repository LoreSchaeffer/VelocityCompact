package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

public class MuteRepository extends EntityRepository<Mute, Long> {

    public MuteRepository(EntityManager entityManager, Class<Mute> entityClass) {
        super(entityManager, entityClass);
    }
}
