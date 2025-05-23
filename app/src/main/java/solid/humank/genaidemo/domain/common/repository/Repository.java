package solid.humank.genaidemo.domain.common.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<A, I> {
    void save(A aggregate);
    Optional<A> findById(I id);
    List<A> findAll();
    void delete(A aggregate);
    boolean exists(I id);
}
