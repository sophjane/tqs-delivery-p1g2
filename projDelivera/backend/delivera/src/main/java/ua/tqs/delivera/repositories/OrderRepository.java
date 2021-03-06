package ua.tqs.delivera.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.tqs.delivera.models.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndOrderState(long id, String state);
}
