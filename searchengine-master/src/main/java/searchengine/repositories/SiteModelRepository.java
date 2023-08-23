package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import java.util.List;

@Repository
public interface SiteModelRepository  extends JpaRepository<SiteModel,Integer /** Тип Id */> {

    List<SiteModel> findByName(String name);
}
