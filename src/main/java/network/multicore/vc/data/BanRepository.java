package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BanRepository extends EntityRepository<Ban, Long> {

    public BanRepository(EntityManager entityManager, Class<Ban> entityClass) {
        super(entityManager, entityClass);
    }

    public List<Ban> findAllActiveByIp(String ip) {
        return entityManager.createQuery("SELECT b FROM Ban b WHERE b.ip = :ip AND b.unbanDate IS NULL", Ban.class)
                .setParameter("ip", ip)
                .getResultList();
    }

    public List<Ban> findAllActiveByUuid(UUID uuid) {
        return entityManager.createQuery("SELECT b FROM Ban b WHERE b.uuid = :uuid AND b.unbanDate IS NULL", Ban.class)
                .setParameter("uuid", uuid.toString())
                .getResultList();
    }

    public List<Ban> findAllActiveByUsername(String username) {
        return entityManager.createQuery("SELECT b FROM Ban b WHERE b.username = :username AND b.unbanDate IS NULL", Ban.class)
                .setParameter("username", username)
                .getResultList();
    }
}
