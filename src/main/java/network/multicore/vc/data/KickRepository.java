package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

import java.util.List;
import java.util.UUID;

public class KickRepository extends EntityRepository<Kick, Long> {

    public KickRepository(EntityManager entityManager, Class<Kick> entityClass) {
        super(entityManager, entityClass);
    }

    public List<Kick> findAllByUuid(UUID uuid) {
        return entityManager.createQuery("SELECT k FROM Kick k WHERE k.uuid = :uuid", entityClass)
                .setParameter("uuid", uuid)
                .getResultList();
    }

    public List<Kick> findAllByIp(String ip) {
        return entityManager.createQuery("SELECT k FROM Kick k WHERE k.ip = :ip", entityClass)
                .setParameter("ip", ip)
                .getResultList();
    }
}
