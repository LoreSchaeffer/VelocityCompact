package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

import java.util.List;
import java.util.UUID;

public class WarnRepository extends EntityRepository<Warn, Long> {

    public WarnRepository(EntityManager entityManager, Class<Warn> entityClass) {
        super(entityManager, entityClass);
    }

    public List<Warn> findAllByUuid(UUID uuid) {
        return entityManager.createQuery("SELECT w FROM Warn w WHERE w.uuid = :uuid", entityClass)
                .setParameter("uuid", uuid)
                .getResultList();
    }
}
