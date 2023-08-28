package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.IndexModel;


public interface IndexRepository extends JpaRepository<IndexModel,Integer> {
}
