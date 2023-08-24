package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;

public interface PageModelRepository extends JpaRepository<PageModel,Integer> {

    List<PageModel> findByPath(String path);
}
