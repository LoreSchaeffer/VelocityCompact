package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

import java.util.List;
import java.util.UUID;

public class MuteRepository extends EntityRepository<Mute, Long> {

    public MuteRepository(EntityManager entityManager, Class<Mute> entityClass) {
        super(entityManager, entityClass);
    }

    public List<Mute> findAllActiveByIp(String ip) {
        return entityManager.createQuery("SELECT m FROM Mute m WHERE m.ip = :ip AND m.unmuteDate IS NULL", Mute.class)
                .setParameter("ip", ip)
                .getResultList();
    }

    public List<Mute> findAllActiveByUuid(UUID uuid) {
        return entityManager.createQuery("SELECT m FROM Mute m WHERE m.uuid = :uuid AND m.unmuteDate IS NULL", Mute.class)
                .setParameter("uuid", uuid.toString())
                .getResultList();
    }

    public List<Mute> findAllActiveByUsername(String username) {
        return entityManager.createQuery("SELECT m FROM Mute m WHERE m.username = :username AND m.unmuteDate IS NULL", Mute.class)
                .setParameter("username", username)
                .getResultList();
    }
}
