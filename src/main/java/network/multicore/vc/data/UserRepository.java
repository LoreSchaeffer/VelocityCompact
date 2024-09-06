package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository extends EntityRepository<User, UUID> {

    public UserRepository(EntityManager entityManager, Class<User> entityClass) {
        super(entityManager, entityClass);
    }

    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public List<User> findAllByIp(String ip) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.ip = :ip", User.class)
                .setParameter("ip", ip)
                .getResultList();
    }
}
