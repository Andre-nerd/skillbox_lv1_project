package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;

import java.util.List;


public interface IndexRepository extends JpaRepository<IndexModel,Integer> {

    List<IndexModel> findByOwnerLemma(Lemma lemma);
}
