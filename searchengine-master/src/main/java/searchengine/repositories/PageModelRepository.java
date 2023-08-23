package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.PageModel;

public interface PageModelRepository extends JpaRepository<PageModel,Integer> {
}
