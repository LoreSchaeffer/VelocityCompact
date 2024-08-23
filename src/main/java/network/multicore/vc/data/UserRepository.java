package network.multicore.vc.data;

import jakarta.persistence.EntityManager;
import network.multicore.vc.persistence.entity.EntityRepository;

import java.util.UUID;

public class UserRepository extends EntityRepository<User, UUID> {

    public UserRepository(EntityManager entityManager, Class<User> entityClass) {
        super(entityManager, entityClass);
    }
}
